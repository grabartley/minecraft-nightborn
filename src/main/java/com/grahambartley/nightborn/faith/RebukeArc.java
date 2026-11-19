package com.grahambartley.nightborn.faith;

import com.grahambartley.nightborn.ward.WornWard;

public final class RebukeArc {

  private RebukeArc() {}

  public static boolean inFront(
      final double lookX,
      final double lookZ,
      final double toX,
      final double toZ,
      final float arcDegrees) {
    final double look = Math.hypot(lookX, lookZ);
    final double to = Math.hypot(toX, toZ);
    if (to < 1.0E-6) {
      return true;
    }
    if (look < 1.0E-6) {
      return false;
    }
    final double cos = (lookX * toX + lookZ * toZ) / (look * to);
    return cos >= Math.cos(Math.toRadians(arcDegrees / 2.0));
  }

  public static boolean armed(final boolean holding, final boolean vampire, final double worn) {
    return WornWard.reach(holding, vampire, worn, 1.0) > 0.0;
  }
}
