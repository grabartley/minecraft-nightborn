package com.grahambartley.nightborn.age;

import com.grahambartley.nightborn.config.AgeSettings;

public record AgeGifts(double health, double damage, int bloodBar) {

  static final int SLOWEST_DRAIN_PERCENT = 90;

  public static AgeGifts of(final int rank, final AgeSettings settings) {
    final int stages = Math.max(0, rank);
    return new AgeGifts(
        (double) stages * settings.stageHealth(),
        (double) stages * settings.stageDamage(),
        stages * settings.stageBlood());
  }

  public static int drainSeconds(final int seconds, final int rank, final int slowerPercent) {
    final int slower =
        Math.max(
            0, Math.min(SLOWEST_DRAIN_PERCENT, Math.max(0, rank) * Math.max(0, slowerPercent)));
    return (int) Math.min(Integer.MAX_VALUE, (long) seconds * 100L / (100 - slower));
  }

  public double healthBeforeMultiplier(final double multiplier) {
    return health / (1.0 + Math.max(0.0, multiplier));
  }
}
