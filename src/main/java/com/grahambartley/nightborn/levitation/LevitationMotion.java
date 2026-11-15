package com.grahambartley.nightborn.levitation;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

public final class LevitationMotion {

  private LevitationMotion() {}

  public static boolean drives(final LivingEntity entity) {
    return Levitation.isLevitating(entity)
        && entity.isLogicalSideForUpdatingMovement()
        && !(entity instanceof ServerPlayerEntity);
  }

  public static void hover(final LivingEntity entity) {
    final Vec3d velocity = entity.getVelocity();
    entity.setVelocity(
        velocity.x,
        LevitationLift.verticalVelocity(entity.getY(), LevitationGround.surfaceBelow(entity)),
        velocity.z);
    entity.fallDistance = 0.0F;
  }

  public static float airSpeed(final LivingEntity entity) {
    return entity.getMovementSpeed() * LevitationLift.AIR_SPEED_FACTOR;
  }
}
