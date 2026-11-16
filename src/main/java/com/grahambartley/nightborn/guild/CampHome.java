package com.grahambartley.nightborn.guild;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public final class CampHome {

  static final String KEY = "Camp";

  private CampHome() {}

  public static void keep(final PathAwareEntity resident, final int reach) {
    if (!resident.hasPositionTarget()) {
      resident.setPositionTarget(resident.getBlockPos(), reach);
    }
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
