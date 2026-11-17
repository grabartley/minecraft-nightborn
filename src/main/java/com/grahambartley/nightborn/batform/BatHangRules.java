package com.grahambartley.nightborn.batform;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class BatHangRules {

  static final double HANG_DROP = 1.0;
  static final double REACH_UP = 3.5;
  static final double REACH_ASIDE = 1.5;
  static final double SLIP = 0.25;

  private BatHangRules() {}

  public static boolean reaches(
      final boolean underside,
      final boolean hanging,
      final double headY,
      final double ceilingY,
      final double aside) {
    final double gap = ceilingY - headY;
    return underside && !hanging && gap >= 0.0 && gap <= REACH_UP && aside <= REACH_ASIDE;
  }

  public static Vec3d spot(final BlockPos anchor) {
    return new Vec3d(anchor.getX() + 0.5, anchor.getY() - HANG_DROP, anchor.getZ() + 0.5);
  }

  public static Vec3d standingFeet(final BlockPos anchor, final double standingHeight) {
    return new Vec3d(anchor.getX() + 0.5, anchor.getY() - standingHeight, anchor.getZ() + 0.5);
  }

  public static boolean slips(final double distanceFromSpot) {
    return distanceFromSpot > SLIP;
  }

  public static boolean armed(
      final boolean hanging, final boolean pressing, final boolean wasArmed) {
    return hanging && (wasArmed || !pressing);
  }

  public static boolean letsGo(final boolean armed, final boolean pressing) {
    return armed && pressing;
  }
}
