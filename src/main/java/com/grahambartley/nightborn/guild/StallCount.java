package com.grahambartley.nightborn.guild;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class StallCount {

  public static final int GIVE_UP_TICKS = 60;
  public static final double PROGRESS = 0.25;

  @Nullable private BlockPos target;
  private int node;
  private double best;
  private int stalled;

  public boolean gaveUp(
      @Nullable final BlockPos heading, final int nodeIndex, final double toNode) {
    if (heading == null || !heading.equals(target) || nodeIndex != node) {
      target = heading;
      node = nodeIndex;
      best = toNode;
      stalled = 0;
      return false;
    }
    if (toNode < best - PROGRESS) {
      best = toNode;
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
