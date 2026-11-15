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

  public static void settle(final LivingEntity sleeper) {
    hold(sleeper, true);
  }

  public static void hold(final LivingEntity sleeper) {
    hold(sleeper, false);
  }

  private static void hold(final LivingEntity sleeper, final boolean settling) {
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
    if (settling && standing) {
      sleeper.setPose(EntityPose.STANDING);
    }
    final Vec3d spot = CoffinLayout.restingSpot(head, facing, standing);
    if (!sleeper.getPos().equals(spot)) {
      sleeper.setPosition(spot);
    }
    sleeper.setVelocity(Vec3d.ZERO);
    if (standing) {
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
