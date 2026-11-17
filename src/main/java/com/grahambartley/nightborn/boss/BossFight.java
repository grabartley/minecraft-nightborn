package com.grahambartley.nightborn.boss;

import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedState;
import com.grahambartley.nightborn.landing.Landing;
import com.grahambartley.nightborn.mob.BossVampireEntity;
import com.grahambartley.nightborn.network.BossArenaPayload;
import com.grahambartley.nightborn.smoke.SmokeParticles;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class BossFight {

  static final float ROAR = 4.0F;
  static final double EDGE = 1.5;
  static final long NEVER = -1L;
  static final String CHANGE_KEY = "BossChangeUntil";
  static final String STAKING_KEY = "BossStaking";
  static final String OCCUPIED_KEY = "BossLastOccupied";
  static final String START_KEY = "BossStart";

  private final PhaseTrack phases = new PhaseTrack();
  private final ArenaRoll roll = new ArenaRoll();
  private final BossAttacks attacks = new BossAttacks();
  private final HitLog hits = new HitLog();
  @Nullable private Vec3d start;
  private long phaseBegan;
  private final BossFlight flight = new BossFlight();
  private final BossAmbience ambience = new BossAmbience();
  private final ArenaSeal seal = new ArenaSeal();
  @Nullable private Arena arena;
  private long changeUntil;
  private boolean staking;
  private long lastOccupied = NEVER;
  @Nullable private BossArenaPayload told;

  public Optional<Arena> arena() {
    return Optional.ofNullable(arena);
  }

  public void claim(final Arena claimed) {
    this.arena = claimed;
  }

  public int phase() {
    return phases.index();
  }

  public boolean busy(final long now) {
    return attacks.busy() || changing(now);
  }

  public boolean untouchable(final long now) {
    return changing(now) || attacks.warded(now);
  }

  public boolean warded(final long now) {
    return attacks.warded(now);
  }

  public boolean draining(final long now) {
    return attacks.draining(now);
  }

  public boolean blazing(final Optional<BossFightPlan> plan) {
    return plan.map(p -> p.phase(phases.index()).body().blazingEyes()).orElse(false);
  }

  public boolean changing(final long now) {
    return now < changeUntil;
  }

  public boolean holds(final Vec3d pos) {
    return arena == null || arena.holds(pos.x, pos.y, pos.z);
  }

  public Optional<PhaseStance> stance(final Optional<BossFightPlan> plan) {
    return plan.map(p -> p.phase(phases.index()).stance());
  }

  public float cap(
      final Optional<BossFightPlan> plan, final boolean finishing, final float amount) {
    return plan.map(p -> p.phase(phases.index()).body().limit(amount, finishing)).orElse(amount);
  }

  public void hold(
      final LivingEntity boss, final Optional<BossFightPlan> plan, final boolean finishing) {
    plan.ifPresent(
        p ->
            boss.setHealth(
                PhaseTrack.held(
                    boss.getHealth(),
                    floorHealth(boss, p),
                    phases.last(p.phases().size()) || finishing)));
  }

  public boolean guards(
      final BossVampireEntity boss,
      final Optional<BossFightPlan> plan,
      @Nullable final Entity attacker) {
    final long now = boss.getWorld().getTime();
    final PhaseDefence defence =
        plan.map(p -> p.phase(phases.index()).defence()).orElse(PhaseDefence.NONE);
    if (attacker == null
        || Downed.state(boss) != null
        || defence.before(hits.hits(now, defence.windowTicks())) != PhaseDefence.Reaction.GUARD) {
      return false;
    }
    hits.clear();
    BossReactions.guard(boss, attacker, defence);
    show(boss, plan.map(p -> p.clips().guard()).orElse(""));
    attacks.counter(boss, defence.counters(), foes(boss), now);
    return true;
  }

  public void hurt(
      final BossVampireEntity boss,
      final Optional<BossFightPlan> plan,
      @Nullable final Entity attacker,
      final float amount) {
    final long now = boss.getWorld().getTime();
    final PhaseDefence defence =
        plan.map(p -> p.phase(phases.index()).defence()).orElse(PhaseDefence.NONE);
    hits.add(now, amount);
    if (attacker instanceof LivingEntity foe
        && boss.canTarget(foe)
        && defence.retargets(foe == boss.getTarget(), boss.getRandom().nextInt(100))) {
      boss.setTarget(foe);
    }
    if (attacker != null
        && !attacks.busy()
        && defence.after(hits.damage(now, defence.windowTicks()))
            == PhaseDefence.Reaction.BREAK_AWAY) {
      hits.clear();
      BossReactions.breakAway(boss, attacker, defence, arena);
      show(boss, plan.map(p -> p.clips().breakAway()).orElse(""));
      attacks.counter(boss, defence.counters(), foes(boss), now);
    }
    attacks.hurt(amount);
    plan.filter(
            p ->
                PhaseTrack.spent(
                    boss.getHealth(), floorHealth(boss, p), phases.last(p.phases().size())))
        .ifPresent(p -> change(boss, p));
  }

  public float floorHealth(final LivingEntity boss, final BossFightPlan plan) {
    return (float) (PhaseTrack.floor(plan.shares(), phases.index()) * boss.getMaxHealth());
  }

  public float ceilingHealth(final LivingEntity boss, final BossFightPlan plan) {
    return (float) (PhaseTrack.ceiling(plan.shares(), phases.index()) * boss.getMaxHealth());
  }

  public void tick(
      final BossVampireEntity boss,
      final Optional<BossFightPlan> plan,
      final FightRules rules,
      final Runnable onReset) {
    if (arena == null || !(boss.getWorld() instanceof ServerWorld world)) {
      return;
    }
    final long now = world.getTime();
    if (start == null) {
      start = boss.getPos();
    }
    plan.ifPresent(p -> embody(boss, p, now));
    final DownedState downed = Downed.state(boss);
    final boolean engaged =
        FightRules.engaged(
            boss.getTarget() != null,
            phases.index(),
            boss.getHealth() < boss.getMaxHealth() - PhaseTrack.SLACK,
            downed != null);
    seal.hold(
        world,
        arena,
        player ->
            !boss.isSleeping()
                && boss.attacksOnSight(player)
                && !player.isCreative()
                && player.canTakeDamage());
    final List<ServerPlayerEntity> inside =
        world.getPlayers(
            player ->
                player.isAlive()
                    && !player.isSpectator()
                    && arena.holds(player.getX(), player.getY(), player.getZ()));
    announce(boss, world, plan, inside, engaged);
    if (!inside.isEmpty() || lastOccupied == NEVER) {
      lastOccupied = now;
    }
    if (rules.resets(engaged, now - lastOccupied, downed != null)) {
      reset(boss);
      onReset.run();
      return;
    }
    keepIn(boss);
    ambience.tick(
        world,
        arena,
        inside,
        plan.flatMap(p -> p.phase(phases.index()).ambience()),
        engaged && downed == null,
        now);
    flight.tick(
        boss,
        plan.map(BossFightPlan::clips).orElse(FightClips.NONE),
        plan.flatMap(p -> p.phase(phases.index()).flight()),
        arena,
        engaged && boss.getTarget() != null,
        downed != null || changing(now),
        attacks.moving(),
        now - attacks.landedAt(),
        now);
    if (finishing(boss, plan, rules, downed) || changing(now) || plan.isEmpty()) {
      return;
    }
    attacks.tick(
        boss,
        plan.get().clips().stagger(),
        plan.get().phase(phases.index()),
        inside.stream().filter(boss::canTarget).toList(),
        ceilingHealth(boss, plan.get()),
        now);
  }

  public void end(final BossVampireEntity boss) {
    attacks.cancel();
    seal.release();
    if (boss.getWorld() instanceof ServerWorld world) {
      send(world, roll.clear(), BossArenaPayload.left(boss.getId()));
    }
    boss.presence().hideAll();
  }

  private void announce(
      final BossVampireEntity boss,
      final ServerWorld world,
      final Optional<BossFightPlan> plan,
      final List<ServerPlayerEntity> inside,
      final boolean engaged) {
    final ArenaRoll.Change change =
        roll.step(inside.stream().map(ServerPlayerEntity::getUuid).collect(Collectors.toSet()));
    final BossArenaPayload here =
        new BossArenaPayload(
            boss.getId(),
            Optional.of(arena),
            engaged,
            plan.flatMap(p -> p.phase(phases.index()).music()));
    for (final ServerPlayerEntity player : inside) {
      if (change.entered().contains(player.getUuid())) {
        boss.presence().show(player);
        ServerPlayNetworking.send(player, here);
      } else if (!here.equals(told)) {
        ServerPlayNetworking.send(player, here);
      }
    }
    for (final UUID gone : change.left()) {
      final ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(gone);
      if (player != null) {
        boss.presence().hide(player);
        ServerPlayNetworking.send(player, BossArenaPayload.left(boss.getId()));
      }
    }
    told = here;
  }

  private static void send(
      final ServerWorld world, final Set<UUID> players, final BossArenaPayload payload) {
    for (final UUID id : players) {
      final ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(id);
      if (player != null) {
        ServerPlayNetworking.send(player, payload);
      }
    }
  }

  private boolean finishing(
      final BossVampireEntity boss,
      final Optional<BossFightPlan> plan,
      final FightRules rules,
      @Nullable final DownedState downed) {
    if (downed == null) {
      staking = false;
      return false;
    }
    if (attacks.busy()) {
      boss.showPower("");
    }
    attacks.cancel();
    if (downed.isDowned()) {
      if (!staking) {
        staking = true;
        boss.setAttached(Downed.TYPE, rules.window(downed));
      }
      return true;
    }
    Downed.clear(boss);
    staking = false;
    final double lastShare =
        plan.map(p -> PhaseTrack.ceiling(p.shares(), p.phases().size() - 1)).orElse(1.0);
    boss.setHealth(rules.risen(boss.getMaxHealth(), lastShare));
    return true;
  }

  public ShownForm shown(final Optional<BossFightPlan> plan, final boolean downed, final long now) {
    return plan.map(
            p ->
                p.phase(phases.index())
                    .look()
                    .shown(flight.flying(), downed, changing(now), p.clips().downedForm()))
        .orElse(ShownForm.NONE)
        .blazing(blazing(plan));
  }

  private static void show(final BossVampireEntity boss, final String clip) {
    if (!clip.isEmpty()) {
      boss.showPower(clip);
    }
  }

  public boolean flying() {
    return flight.flying();
  }

  private List<ServerPlayerEntity> foes(final BossVampireEntity boss) {
    return boss.getWorld() instanceof ServerWorld world && arena != null
        ? world.getPlayers(
            player ->
                player.isAlive()
                    && arena.holds(player.getX(), player.getY(), player.getZ())
                    && boss.canTarget(player))
        : List.of();
  }

  private void embody(final BossVampireEntity boss, final BossFightPlan plan, final long now) {
    final int index = phases.index();
    final double progress = PhaseBodies.progress(changeUntil, now, plan.changeTicks());
    PhaseBodies.apply(boss, plan, index, progress);
    PhaseArms.hold(boss, plan.phase(index).look().held());
    plan.phase(index)
        .voice()
        .filter(voice -> boss.getTarget() != null && voice.speaksAt(now - phaseBegan))
        .ifPresent(voice -> boss.playSound(SoundEvent.of(voice.sound()), ROAR, voice.pitch()));
  }

  private void keepIn(final BossVampireEntity boss) {
    final Vec3d pos = boss.getPos();
    if (arena.holds(pos.x, pos.y, pos.z)) {
      return;
    }
    final Vec3d back = arena.pullIn(pos, EDGE);
    Landing.reach(boss, back.add(0.0, boss.getStandingEyeHeight(), 0.0))
        .ifPresentOrElse(
            to -> Landing.move(boss, to), () -> boss.requestTeleport(back.x, back.y, back.z));
    boss.getNavigation().stop();
  }

  private void change(final BossVampireEntity boss, final BossFightPlan plan) {
    phases.advance(plan.phases().size());
    changeUntil = boss.getWorld().getTime() + plan.changeTicks();
    phaseBegan = boss.getWorld().getTime();
    attacks.cancel();
    hits.clear();
    BossReactions.returnTo(boss, start);
    BossReactions.strike(boss);
    boss.getNavigation().stop();
    boss.showPower(plan.phase(phases.index()).look().enters(plan.changeClip()));
    boss.playSound(SoundEvent.of(plan.changeSound()), ROAR, 0.8F);
    if (boss.getWorld() instanceof ServerWorld world) {
      world.spawnParticles(
          SmokeParticles.VAMPIRE_SMOKE,
          boss.getX(),
          boss.getBodyY(0.5),
          boss.getZ(),
          80,
          plan.changeRange() / 3.0,
          0.6,
          plan.changeRange() / 3.0,
          0.08);
      for (final LivingEntity near :
          world.getEntitiesByClass(
              LivingEntity.class,
              boss.getBoundingBox().expand(plan.changeRange()),
              entity -> entity != boss && entity.isAlive() && boss.canTarget(entity))) {
        near.takeKnockback(
            plan.changeKnock(), boss.getX() - near.getX(), boss.getZ() - near.getZ());
        near.velocityModified = true;
      }
    }
  }

  private void reset(final BossVampireEntity boss) {
    phases.reset();
    attacks.cancel();
    hits.clear();
    changeUntil = 0L;
    BossReactions.returnTo(boss, start);
    boss.setTarget(null);
    boss.getNavigation().stop();
    boss.setHealth(boss.getMaxHealth());
  }

  public void write(final NbtCompound nbt) {
    phases.write(nbt);
    nbt.putLong(CHANGE_KEY, changeUntil);
    nbt.putBoolean(STAKING_KEY, staking);
    nbt.putLong(OCCUPIED_KEY, lastOccupied);
    if (start != null) {
      nbt.put(START_KEY, SpotNbt.list(start));
    }
    if (arena != null) {
      arena.write(nbt);
    }
  }

  public void read(final NbtCompound nbt) {
    phases.read(nbt);
    changeUntil = nbt.getLong(CHANGE_KEY);
    staking = nbt.getBoolean(STAKING_KEY);
    lastOccupied = nbt.contains(OCCUPIED_KEY) ? nbt.getLong(OCCUPIED_KEY) : NEVER;
    start = SpotNbt.vec(nbt, START_KEY).orElse(start);
    arena = Arena.read(nbt).orElse(arena);
  }
}
