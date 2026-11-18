package com.grahambartley.nightborn.raid;

import com.grahambartley.nightborn.faction.GuildMembership;
import com.grahambartley.nightborn.gore.GoreParticles;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public final class Trails {

  static final int DROPS = 3;

  private static final Map<UUID, Trail> TRAILS = new HashMap<>();

  private Trails() {}

  static void register() {
    ServerLivingEntityEvents.AFTER_DAMAGE.register(
        (entity, source, base, taken, blocked) -> wounded(entity, source, taken));
    ServerPlayConnectionEvents.DISCONNECT.register(
        (handler, server) -> TRAILS.remove(handler.getPlayer().getUuid()));
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> TRAILS.clear());
  }

  public static Optional<Vec3d> lead(final ServerPlayerEntity hunter) {
    final Trail trail = TRAILS.get(hunter.getUuid());
    if (trail == null) {
      return Optional.empty();
    }
    final ServerWorld world = hunter.getServerWorld();
    if (TrailRules.over(world.getTime(), trail.until())) {
      TRAILS.remove(hunter.getUuid());
      return Optional.empty();
    }
    if (!world.getRegistryKey().equals(trail.world())) {
      return Optional.empty();
    }
    final Entity quarry = world.getEntity(trail.quarry());
    if (quarry instanceof LivingEntity living) {
      if (!living.isAlive()) {
        TRAILS.remove(hunter.getUuid());
        return Optional.empty();
      }
      drip(world, hunter, living);
      return Optional.of(living.getPos());
    }
    return trail.coffin().map(Vec3d::ofBottomCenter);
  }

  private static void wounded(
      final LivingEntity victim, final DamageSource source, final float taken) {
    if (taken <= 0.0F
        || !(victim instanceof VampireEntity vampire)
        || !victim.isAlive()
        || !(source.getAttacker() instanceof ServerPlayerEntity hunter)
        || Vampires.isVampire(hunter)
        || !GuildMembership.isHunter(hunter)) {
      return;
    }
    final ServerWorld world = hunter.getServerWorld();
    TRAILS.put(
        hunter.getUuid(),
        new Trail(
            vampire.getUuid(),
            vampire.homeLife().coffin(),
            world.getRegistryKey(),
            TrailRules.until(world.getTime(), world.getTimeOfDay())));
  }

  private static void drip(
      final ServerWorld world, final ServerPlayerEntity hunter, final LivingEntity quarry) {
    world.spawnParticles(
        hunter,
        GoreParticles.DROP,
        false,
        quarry.getX(),
        quarry.getY() + 0.1,
        quarry.getZ(),
        DROPS,
        0.25,
        0.0,
        0.25,
        0.0);
  }
}
