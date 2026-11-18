package com.grahambartley.nightborn.ward;

public final class WornWard {

  private WornWard() {}

  public static double reach(
      final boolean holding, final boolean vampire, final double wornShare, final double range) {
    if (vampire) {
      return 0.0;
    }
    if (holding) {
      return range;
    }
    return range * Math.max(0.0, Math.min(1.0, wornShare));
  }
}
