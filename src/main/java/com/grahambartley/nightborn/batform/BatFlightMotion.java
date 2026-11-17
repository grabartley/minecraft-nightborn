package com.grahambartley.nightborn.batform;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

public final class BatFlightMotion {

  private BatFlightMotion() {}

  public static boolean drives(final LivingEntity entity) {
    return drives(
        BatForm.isBat(entity),
        entity instanceof PlayerEntity && !(entity instanceof ServerPlayerEntity),
        entity.isLogicalSideForUpdatingMovement(),
        entity.isTouchingWater() || entity.isInLava());
  }

  static boolean drives(
      final boolean bat,
      final boolean clientPlayer,
      final boolean movesHere,
      final boolean inFluid) {
    return bat && clientPlayer && movesHere && !inFluid;
  }

  public static void travel(
      final LivingEntity entity, final Vec3d movementInput, final boolean jumping) {
    final Vec3d velocity =
        BatHang.isHanging(entity)
            ? Vec3d.ZERO
            : BatFlightModel.next(
                entity.getVelocity(),
                entity.isOnGround(),
                new BatFlightModel.Controls(
                    entity.getRotationVector(),
                    entity.getYaw(),
                    movementInput.z,
                    movementInput.x,
                    entity.isSprinting(),
                    jumping));
    entity.setVelocity(velocity);
    entity.move(MovementType.SELF, velocity);
    entity.fallDistance = 0.0F;
    entity.updateLimbs(false);
  }
}
