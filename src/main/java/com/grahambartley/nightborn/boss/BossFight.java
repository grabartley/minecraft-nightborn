package com.grahambartley.nightborn.boss;

import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedState;
import com.grahambartley.nightborn.gear.Staking;
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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class BossFight {

  static final float ROAR = 4.0F;
  static final double EDGE = 1.5;

  private final PhaseTrack phases = new PhaseTrack();
  private final ArenaRoll roll = new ArenaRoll();
  private final BossAttacks attacks = new BossAttacks();
  @Nullable private Arena arena;
  private long changeUntil;
  private boolean staking;
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

  public boolean changing(final long now) {
    return now < changeUntil;
  }

  public boolean holds(final Vec3d pos) {
    return arena == null || arena.holds(pos.x, pos.y, pos.z);
  }

  public boolean casts(final Optional<BossFightPlan> plan, final Identifier power) {
    return plan.map(p -> p.phase(phases.index()).casts(power)).orElse(true);
  }

  public float cap(
      final LivingEntity boss,
      final Optional<BossFightPlan> plan,
      final DamageSource source,
      final float amount) {
    return plan.map(
            p ->
                PhaseTrack.cap(
                    amount,
                    boss.getHealth(),
                    floorHealth(boss, p),
                    phases.last(p.phases().size()) || Staking.finishesVampire(source)))
        .orElse(amount);
  }

  public void hurt(
      final BossVampireEntity boss, final Optional<BossFightPlan> plan, final float amount) {
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
    final DownedState downed = Downed.state(boss);
    final boolean engaged =
        FightRules.engaged(
            boss.getTarget() != null,
            phases.index(),
            boss.getHealth() < boss.getMaxHealth() - PhaseTrack.SLACK,
            downed != null);
    final List<ServerPlayerEntity> inside =
        world.getPlayers(
            player ->
                player.isAlive()
                    && !player.isSpectator()
                    && arena.holds(player.getX(), player.getY(), player.getZ()));
    announce(boss, world, plan, inside, engaged);
    if (rules.resets(engaged, roll.emptyFor(rules.emptyTicks()), downed != null)) {
      reset(boss);
      onReset.run();
      return;
    }
    keepIn(boss);
    if (finishing(boss, plan, rules, downed) || changing(now) || plan.isEmpty()) {
      return;
    }
    attacks.tick(
        boss,
        plan.get().phase(phases.index()),
        inside.stream().filter(boss::canTarget).toList(),
        ceilingHealth(boss, plan.get()),
        now);
  }

  public void end(final BossVampireEntity boss) {
    attacks.cancel();
    if (boss.getWorld() instanceof ServerWorld world) {
      send(world, roll.clear(), BossArenaPayload.left());
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
            Optional.of(arena), engaged, plan.flatMap(p -> p.phase(phases.index()).music()));
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
        ServerPlayNetworking.send(player, BossArenaPayload.left());
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
    attacks.cancel();
    boss.getNavigation().stop();
    boss.showPower(plan.changeClip());
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
    changeUntil = 0L;
    boss.setTarget(null);
    boss.getNavigation().stop();
    boss.setHealth(boss.getMaxHealth());
  }

  public void write(final NbtCompound nbt) {
    phases.write(nbt);
    if (arena != null) {
      arena.write(nbt);
    }
  }

  public void read(final NbtCompound nbt) {
    phases.read(nbt);
    arena = Arena.read(nbt).orElse(arena);
  }
}
