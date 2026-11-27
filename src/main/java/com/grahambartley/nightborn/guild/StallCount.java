package com.grahambartley.nightborn.guild;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class StallCount {

  public static final int GIVE_UP_TICKS = 60;
  public static final double PROGRESS = 1.0;

  @Nullable private BlockPos target;
  private Vec3d anchor = Vec3d.ZERO;
  private int stalled;

  public boolean gaveUp(@Nullable final BlockPos heading, final Vec3d pos) {
    if (heading == null || !heading.equals(target)) {
      target = heading;
      anchor = pos;
      stalled = 0;
      return false;
    }
    if (pos.subtract(anchor).horizontalLength() > PROGRESS) {
      anchor = pos;
      stalled = 0;
      return false;
    }
    if (++stalled < GIVE_UP_TICKS) {
      return false;
    }
    target = null;
    stalled = 0;
    return true;
  }
}
