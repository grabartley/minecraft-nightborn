package com.grahambartley.nightborn.faith;

import com.grahambartley.nightborn.particle.ModParticles;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Arm;
import net.minecraft.util.math.Vec3d;

public final class BladeFlames {

  public static final SimpleParticleType FLAME = ModParticles.simple("blood_flame");

  static final double START = 0.2;
  static final double SPREAD = 0.04;
  static final int PER_PULSE = 3;

  private BladeFlames() {}

  static void register() {}

  static void emit(final ServerWorld world, final LivingEntity holder) {
    final boolean right = holder.getMainArm() == Arm.RIGHT;
    for (int i = 0; i < PER_PULSE; i++) {
      final double t = START + (1.0 - START) * holder.getRandom().nextDouble();
      final Vec3d at =
          holder.getPos().add(BladePoints.along(holder.getBodyYaw(), right, holder.getHeight(), t));
      world.spawnParticles(FLAME, at.x, at.y, at.z, 1, SPREAD, SPREAD, SPREAD, 0.0);
    }
  }
}
