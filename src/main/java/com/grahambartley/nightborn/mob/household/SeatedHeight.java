package com.grahambartley.nightborn.mob.household;

public final class SeatedHeight {

  static final double HIP_SHARE = 12.0 / 32.0;
  static final double CUSHION = 0.05;

  private SeatedHeight() {}

  public static double lift(final float height, final float scale) {
    return height * scale * HIP_SHARE - CUSHION;
  }
}
