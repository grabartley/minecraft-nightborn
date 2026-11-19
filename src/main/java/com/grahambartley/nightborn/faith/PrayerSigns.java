package com.grahambartley.nightborn.faith;

import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

final class PrayerSigns {

  static final DustParticleEffect GOLD_DUST =
      new DustParticleEffect(new Vector3f(0.95F, 0.82F, 0.45F), 1.0F);

  private PrayerSigns() {}

  static void mended(final ServerWorld world, final LivingEntity human) {
    world.spawnParticles(
        ParticleTypes.HEART,
        human.getX(),
        human.getBodyY(1.0),
        human.getZ(),
        2,
        0.3,
        0.2,
        0.3,
        0.0);
  }

  static void glow(
      final ServerWorld world, final Vec3d centre, final PrayerPower power, final boolean pillar) {
    if (!pillar) {
      ring(world, centre, power.radius());
      return;
    }
    final double spread = power.radius() * 0.4;
    for (double y = 0.2; y < power.height(); y += 0.8) {
      world.spawnParticles(
          ParticleTypes.END_ROD, centre.x, centre.y + y, centre.z, 2, spread, 0.2, spread, 0.0);
    }
  }

  static void ring(final ServerWorld world, final Vec3d centre, final float radius) {
    final int points = (int) Math.ceil(radius * 6);
    for (int i = 0; i < points; i++) {
      final double angle = Math.PI * 2 * i / points;
      world.spawnParticles(
          GOLD_DUST,
          centre.x + Math.cos(angle) * radius,
          centre.y + 0.15,
          centre.z + Math.sin(angle) * radius,
          1,
          0.0,
          0.0,
          0.0,
          0.0);
    }
  }
}
