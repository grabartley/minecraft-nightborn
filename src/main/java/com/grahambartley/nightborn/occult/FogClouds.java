package com.grahambartley.nightborn.occult;

import com.grahambartley.nightborn.servant.Servants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class FogClouds {

  static final int PARTICLE_EVERY_TICKS = 2;

  private record Cloud(
      RegistryKey<World> world, Vec3d centre, UUID caster, FogPower power, long startedAt) {}

  private static final List<Cloud> CLOUDS = new ArrayList<>();

  private FogClouds() {}

  static void register() {
    ServerTickEvents.END_WORLD_TICK.register(FogClouds::tick);
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> clearAll());
  }

  public static void clearAll() {
    CLOUDS.clear();
  }

  public static void spawn(final LivingEntity caster, final FogPower power) {
    if (caster.getWorld() instanceof ServerWorld world) {
      CLOUDS.add(
          new Cloud(
              world.getRegistryKey(), caster.getPos(), caster.getUuid(), power, world.getTime()));
    }
  }

  private static void tick(final ServerWorld world) {
    final long now = world.getTime();
    final Iterator<Cloud> clouds = CLOUDS.iterator();
    while (clouds.hasNext()) {
      final Cloud cloud = clouds.next();
      if (!cloud.world().equals(world.getRegistryKey())) {
        continue;
      }
      if (!FogRules.lasts(now, cloud.startedAt() + cloud.power().durationTicks())) {
        clouds.remove();
        continue;
      }
      if (now % PARTICLE_EVERY_TICKS == 0) {
        drift(world, cloud);
      }
      if (FogRules.pulses(now, cloud.startedAt())) {
        shroud(world, cloud);
      }
    }
  }

  private static void drift(final ServerWorld world, final Cloud cloud) {
    final double radius = cloud.power().radius();
    world.spawnParticles(
        ParticleTypes.WHITE_SMOKE,
        cloud.centre().x,
        cloud.centre().y + 0.8,
        cloud.centre().z,
        (int) Math.ceil(radius * 3),
        radius * 0.6,
        0.6,
        radius * 0.6,
        0.005);
    world.spawnParticles(
        ParticleTypes.LARGE_SMOKE,
        cloud.centre().x,
        cloud.centre().y + 0.4,
        cloud.centre().z,
        (int) Math.ceil(radius),
        radius * 0.5,
        0.3,
        radius * 0.5,
        0.002);
  }

  private static void shroud(final ServerWorld world, final Cloud cloud) {
    final float radius = cloud.power().radius();
    final Entity caster = world.getEntity(cloud.caster());
    for (final LivingEntity living :
        world.getEntitiesByClass(
            LivingEntity.class,
            new Box(cloud.centre(), cloud.centre()).expand(radius),
            candidate ->
                FogRules.covers(candidate.squaredDistanceTo(cloud.centre()), radius)
                    && FogRules.touches(
                        candidate.isAlive(),
                        candidate.getUuid().equals(cloud.caster()),
                        Servants.serves(Servants.masterOf(candidate), cloud.caster()),
                        candidate.isSpectator()))) {
      for (final StatusEffectInstance effect : cloud.power().effects()) {
        living.addStatusEffect(new StatusEffectInstance(effect), caster);
      }
    }
  }
}
