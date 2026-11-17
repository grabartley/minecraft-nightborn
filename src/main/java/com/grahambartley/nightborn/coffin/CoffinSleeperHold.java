package com.grahambartley.nightborn.coffin;

import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class CoffinSleeperHold {

  private CoffinSleeperHold() {}

  public static void hold(final LivingEntity sleeper) {
    final BlockPos head = sleeper.getSleepingPosition().orElse(null);
    if (head == null) {
      return;
    }
    final BlockState state = sleeper.getWorld().getBlockState(head);
    if (!(state.getBlock() instanceof CoffinBlock)) {
      return;
    }
    final Direction facing = state.get(BedBlock.FACING);
    final boolean standing = state.get(CoffinBlock.STANDING);
    final EntityPose pose = CoffinLayout.restingPose(standing, sleeper.getPose());
    if (pose != sleeper.getPose()) {
      sleeper.setPose(pose);
    }
    final boolean rising = CoffinRise.rising(sleeper);
    final Vec3d spot =
        CoffinLayout.holdSpot(
            head, facing, standing, rising, sleeper.getEyeHeight(EntityPose.STANDING));
    if (!sleeper.getPos().equals(spot)) {
      sleeper.setPosition(spot);
    }
    sleeper.setVelocity(Vec3d.ZERO);
    if (CoffinLayout.facesOut(standing, rising)) {
      faceOut(sleeper, CoffinLayout.outwardYaw(facing));
    }
  }

  private static void faceOut(final LivingEntity sleeper, final float yaw) {
    sleeper.setYaw(yaw);
    sleeper.prevYaw = yaw;
    sleeper.setBodyYaw(yaw);
    sleeper.prevBodyYaw = yaw;
    sleeper.setHeadYaw(yaw);
    sleeper.prevHeadYaw = yaw;
  }
}
