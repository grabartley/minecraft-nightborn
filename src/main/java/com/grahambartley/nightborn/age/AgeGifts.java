package com.grahambartley.nightborn.age;

import com.grahambartley.nightborn.config.AgeSettings;

public final class AgeGifts {

  static final int SLOWEST_DRAIN_PERCENT = 90;

  private AgeGifts() {}

  public static int bloodBar(final int rank, final AgeSettings settings) {
    return Math.max(0, rank) * settings.stageBlood();
  }

  public static int drainSeconds(final int seconds, final int rank, final int slowerPercent) {
    final int slower =
        Math.max(
            0, Math.min(SLOWEST_DRAIN_PERCENT, Math.max(0, rank) * Math.max(0, slowerPercent)));
    return (int) Math.min(Integer.MAX_VALUE, (long) seconds * 100L / (100 - slower));
  }
}
