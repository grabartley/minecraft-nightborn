package com.grahambartley.nightborn.boss;

import com.grahambartley.nightborn.landing.Landing;
import com.grahambartley.nightborn.mind.Gazes;
import com.grahambartley.nightborn.occult.ShadowStep;
import com.grahambartley.nightborn.skill.PowerShow;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class BossAttacks {

  static final float LOUD = 2.0F;

  private record Hazard(
      ZoneAttack zone, ZoneShape shape, double y, ZoneMarkEntity mark, long from) {}

  private final Map<Identifier, Long> readyAt = new HashMap<>();
  private final List<ZoneShape> shapes = new ArrayList<>();
  private final List<ZoneMarkEntity> marks = new ArrayList<>();
  private final List<Hazard> hazards = new ArrayList<>();
  @Nullable private AttackRun run;
  @Nullable private Vec3d leapFrom;
  @Nullable private Vec3d leapTo;
  private double markY;
  private float drainTaken;
  private long nextAttack;
  private boolean cut;
  private long landedAt = Long.MIN_VALUE / 2;
  private final BossWard ward = new BossWard();
  private final BossWard court = new BossWard();

  public boolean busy() {
    return run != null;
  }

  public boolean moving() {
    return run != null && run.moves();
  }

  public long landedAt() {
    return landedAt;
  }

  public boolean warded(final long now) {
    return ward.holds(now);
  }

  public boolean draining(final long now) {
    return run != null && run.drains(now);
  }

  public <T extends MobEntity & PowerShow> void counter(
      final T boss,
      final List<Identifier> counters,
      final List<? extends LivingEntity> foes,
      final long now) {
    final LivingEntity target = boss.getTarget();
    if (run != null
        || target == null
        || counters.isEmpty()
        || !(boss.getWorld() instanceof ServerWorld world)) {
      return;
    }
    final List<AttackChoice.Option> options = new ArrayList<>();
    for (final Identifier id : counters) {
      BossFights.attack(world.getRegistryManager(), id)
          .ifPresent(
              attack ->
                  options.add(new AttackChoice.Option(id, attack, readyAt.getOrDefault(id, 0L))));
    }
    AttackChoice.choose(options, boss.distanceTo(target), now, boss.getRandom()::nextInt)
        .ifPresent(option -> begin(boss, world, option, target, foes, now));
  }

  public void hurt(final float amount) {
    drainTaken += amount;
  }

  public <T extends MobEntity & PowerShow> void tick(
      final T boss,
      final BossPhase phase,
      final List<? extends LivingEntity> foes,
      final float healCeiling,
      final long now) {
    if (!(boss.getWorld() instanceof ServerWorld world)) {
      return;
    }
    tickHazards(boss, world, now);
    ward.expire(now);
    court.expire(now);
    if (run == null) {
      choose(boss, world, phase, foes, now);
      return;
    }
    switch (cut ? AttackRun.Stage.DONE : run.stage(now)) {
      case TELL -> tell(boss, now);
      case STRIKE -> strike(boss, world, healCeiling, now);
      case DONE -> finish(phase, now);
    }
  }

  public void cancel() {
    marks.forEach(ZoneMarkEntity::discard);
    marks.clear();
    shapes.clear();
    hazards.forEach(hazard -> hazard.mark().discard());
    hazards.clear();
    ward.drop();
    court.drop();
    run = null;
    cut = false;
    leapFrom = null;
    leapTo = null;
  }

  private <T extends MobEntity & PowerShow> void choose(
      final T boss,
      final ServerWorld world,
      final BossPhase phase,
      final List<? extends LivingEntity> foes,
      final long now) {
    final LivingEntity target = boss.getTarget();
    if (target == null || now < nextAttack) {
      return;
    }
    final List<AttackChoice.Option> options = new ArrayList<>();
    for (final Identifier id : phase.attacks()) {
      BossFights.attack(world.getRegistryManager(), id)
          .ifPresent(
              attack ->
                  options.add(new AttackChoice.Option(id, attack, readyAt.getOrDefault(id, 0L))));
    }
    AttackChoice.choose(options, boss.distanceTo(target), now, boss.getRandom()::nextInt)
        .ifPresent(option -> begin(boss, world, option, target, foes, now));
  }

  private <T extends MobEntity & PowerShow> void begin(
      final T boss,
      final ServerWorld world,
      final AttackChoice.Option option,
      final LivingEntity target,
      final List<? extends LivingEntity> foes,
      final long now) {
    final BossAttack attack = option.attack();
    run = new AttackRun(option.id(), attack, now);
    drainTaken = 0.0F;
    boss.getNavigation().stop();
    boss.showPower(attack.tellClip());
    boss.playSound(SoundEvent.of(attack.tellSound()), LOUD, 1.0F);
    attack.zone().ifPresent(zone -> mark(boss, world, zone, target, foes));
  }

  private void mark(
      final MobEntity boss,
      final ServerWorld world,
      final ZoneAttack zone,
      final LivingEntity target,
      final List<? extends LivingEntity> foes) {
    final List<? extends LivingEntity> anchors =
        zone.anchor() == ZoneAttack.Anchor.EVERY_FOE && !foes.isEmpty() ? foes : List.of(target);
    markY = ZoneStrikes.floor(world, zone.anchor() == ZoneAttack.Anchor.SELF ? boss : target);
    for (final LivingEntity anchor : anchors) {
      final ZoneShape shape = zone.at(boss.getX(), boss.getZ(), anchor.getX(), anchor.getZ());
      shapes.add(shape);
      marks.add(ZoneMarkEntity.place(world, shape, ZoneStrikes.floor(world, anchor) + 0.02));
    }
    if (zone.motion() == ZoneAttack.Motion.LEAP) {
      final ZoneShape first = shapes.get(0);
      leapFrom = boss.getPos();
      leapTo = new Vec3d(first.endX(), markY, first.endZ());
    }
  }

  private void tell(final MobEntity boss, final long now) {
    if (run == null || leapFrom == null || leapTo == null) {
      return;
    }
    final float lift = run.attack().zone().map(ZoneAttack::lift).orElse(0.0F);
    boss.setVelocity(LeapArc.step(leapFrom, leapTo, lift, run.age(now), run.attack().tellTicks()));
    boss.velocityModified = true;
    boss.fallDistance = 0.0F;
  }

  private <T extends MobEntity & PowerShow> void strike(
      final T boss, final ServerWorld world, final float healCeiling, final long now) {
    if (run == null) {
      return;
    }
    final BossAttack attack = run.attack();
    final int tick = run.strikeTick(now);
    if (tick == 0) {
      boss.showPower(attack.clip());
      boss.playSound(SoundEvent.of(attack.sound()), LOUD, 1.0F);
      attack.power().ifPresent(power -> BossPowers.cast(boss, power));
      attack
          .ward()
          .ifPresent(
              keepers -> (keepers.shields() ? ward : court).raise(boss, world, keepers, now));
      attack
          .gaze()
          .filter(gaze -> boss.getTarget() != null)
          .ifPresent(gaze -> Gazes.start(boss, boss.getTarget(), gaze));
      attack.zone().ifPresent(zone -> burst(boss, world, zone, now));
    }
    attack
        .flurry()
        .filter(flurry -> flurry.hitsAt(tick))
        .ifPresent(flurry -> FlurryStrikes.hit(boss, world, flurry));
    attack.drain().ifPresent(drain -> drain(boss, world, drain, healCeiling, tick));
  }

  private void burst(
      final MobEntity boss, final ServerWorld world, final ZoneAttack zone, final long now) {
    if (zone.motion() != ZoneAttack.Motion.STAY && !shapes.isEmpty()) {
      final ZoneShape first = shapes.get(0);
      final Vec3d wanted =
          new Vec3d(first.endX(), markY + boss.getStandingEyeHeight(), first.endZ());
      Landing.reach(boss, wanted)
          .ifPresent(
              to -> {
                if (zone.motion() == ZoneAttack.Motion.STEP) {
                  ShadowStep.step(boss, to);
                } else {
                  Landing.move(boss, to);
                }
              });
      boss.setVelocity(Vec3d.ZERO);
      landedAt = now;
    }
    for (int i = 0; i < shapes.size(); i++) {
      final ZoneShape shape = shapes.get(i);
      final ZoneMarkEntity mark = marks.get(i);
      ZoneStrikes.harm(boss, world, shape, mark.getY(), zone.damage(), zone.knock());
      ZoneStrikes.spray(world, shape, mark.getY());
      if (zone.lingers()) {
        mark.linger(zone.lingerTicks());
        hazards.add(new Hazard(zone, shape, mark.getY(), mark, now));
      } else {
        mark.discard();
      }
    }
    marks.clear();
    shapes.clear();
    leapFrom = null;
    leapTo = null;
  }

  private void tickHazards(final MobEntity boss, final ServerWorld world, final long now) {
    hazards.removeIf(hazard -> now - hazard.from() > hazard.zone().lingerTicks());
    for (final Hazard hazard : hazards) {
      if (hazard.zone().hurtsAt((int) (now - hazard.from()))) {
        ZoneStrikes.harm(boss, world, hazard.shape(), hazard.y(), hazard.zone().damage(), 0.0F);
      }
    }
  }

  private void drain(
      final MobEntity boss,
      final ServerWorld world,
      final DrainAttack drain,
      final float healCeiling,
      final int tick) {
    final LivingEntity foe = boss.getTarget();
    final boolean alive = foe != null && foe.isAlive() && boss.canSee(foe);
    final DrainAttack.Step step =
        drain.step(tick, drainTaken, alive, foe == null ? 0.0 : boss.distanceTo(foe));
    if (step == DrainAttack.Step.PULSE && foe != null) {
      DrainChannel.pulse(boss, world, foe, drain, healCeiling);
    }
    if (step == DrainAttack.Step.BROKEN) {
      DrainChannel.snap(boss, world);
    }
    cut = step == DrainAttack.Step.BROKEN || step == DrainAttack.Step.DONE;
  }

  private void finish(final BossPhase phase, final long now) {
    if (run != null) {
      readyAt.put(run.id(), now + run.attack().cooldownTicks());
    }
    nextAttack = now + phase.gapTicks();
    marks.forEach(ZoneMarkEntity::discard);
    marks.clear();
    shapes.clear();
    run = null;
    cut = false;
    leapFrom = null;
    leapTo = null;
  }
}
