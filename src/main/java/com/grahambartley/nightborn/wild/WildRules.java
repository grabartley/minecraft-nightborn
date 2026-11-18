package com.grahambartley.nightborn.wild;

import java.util.List;
import net.minecraft.util.math.BlockPos;

public final class WildRules {

  public static final int PERCENT = 100;

  private WildRules() {}

  public static boolean rolls(final int roll, final int chance) {
    return roll < chance;
  }

  public static boolean farFromEvery(
      final BlockPos spot, final List<BlockPos> players, final int distance) {
    final double reach = (double) distance * distance;
    return players.stream().allMatch(player -> flatDistanceSquared(spot, player) >= reach);
  }

  public static boolean watcherLeaves(
      final boolean watchedPresent,
      final double distanceSquared,
      final int leaveDistance,
      final boolean night,
      final boolean struck) {
    return !watchedPresent
        || distanceSquared <= (double) leaveDistance * leaveDistance
        || !night
        || struck;
  }

  public static boolean backsOff(final double distanceSquared, final int keep) {
    return distanceSquared < (double) keep * keep;
  }

  static double flatDistanceSquared(final BlockPos from, final BlockPos to) {
    final double dx = to.getX() - from.getX();
    final double dz = to.getZ() - from.getZ();
    return dx * dx + dz * dz;
  }
}
