package com.grahambartley.nightborn.levitation;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public final class LevitationMotion {

  private LevitationMotion() {}

  public static boolean drives(final LivingEntity entity) {
    return drives(Levitation.isLevitating(entity), entity.isLogicalSideForUpdatingMovement());
  }

  static boolean drives(final boolean levitating, final boolean movesHere) {
    return levitating && movesHere;
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
