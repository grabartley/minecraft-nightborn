package com.grahambartley.nightborn.hunter;

public final class FaithBoost {

  private FaithBoost() {}

  public static double multiplier(final double faith, final int bonusPercent) {
    return 1.0 + Math.max(0.0, faith) * Math.max(0, bonusPercent) / 100.0;
  }
}
