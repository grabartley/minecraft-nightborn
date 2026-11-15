package com.grahambartley.nightborn.decor;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public final class DecorVariants {

  private DecorVariants() {}

  public static int pick(final BlockPos pos, final int count) {
    if (count < 1) {
      throw new IllegalArgumentException("count must be positive, was " + count);
    }
    return (int) Math.floorMod(MathHelper.hashCode(pos) >>> 16, (long) count);
  }
}
