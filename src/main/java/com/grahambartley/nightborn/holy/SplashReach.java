package com.grahambartley.nightborn.holy;

public final class SplashReach {

  private SplashReach() {}

  public static double reach(final double base, final boolean holyWater, final double extra) {
    return holyWater ? base + Math.max(0.0, extra) : base;
  }

  public static double reachSquared(
      final double baseSquared, final boolean holyWater, final double extra) {
    final double reach = reach(Math.sqrt(baseSquared), holyWater, extra);
    return reach * reach;
  }
}
