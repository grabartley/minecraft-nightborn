package com.grahambartley.nightborn.guild;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public final class CampHome {

  static final String KEY = "Camp";
  static final float UNBOUNDED = -1.0F;

  private CampHome() {}

  public static void keep(final PathAwareEntity resident, final int reach) {
    if (!resident.hasPositionTarget()) {
      resident.setPositionTarget(resident.getBlockPos(), reach);
    }
  }

  public static boolean reaches(
      final float range, final double squaredDistance, final int fighting, final boolean inFight) {
    if (range == UNBOUNDED) {
      return true;
    }
    final double reach = inFight ? Math.max(range, fighting) : range;
    return squaredDistance < reach * reach;
  }

  public static void write(final PathAwareEntity resident, final NbtCompound nbt) {
    if (resident.hasPositionTarget()) {
      nbt.putLong(KEY, resident.getPositionTarget().asLong());
    }
  }

  public static void read(final PathAwareEntity resident, final NbtCompound nbt, final int reach) {
    if (nbt.contains(KEY)) {
      resident.setPositionTarget(BlockPos.fromLong(nbt.getLong(KEY)), reach);
    }
  }
}
