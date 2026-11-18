package com.grahambartley.nightborn.raid;

public final class Noise {

  static final double TICKS_PER_SECOND = 20.0;

  private Noise() {}

  public static double faded(final double level, final long elapsedTicks, final int fadePerSecond) {
    final double lost = fadePerSecond * Math.max(0L, elapsedTicks) / TICKS_PER_SECOND;
    return Math.max(0.0, level - lost);
  }

  public static double heard(
      final double level, final long elapsedTicks, final int fadePerSecond, final double loudness) {
    return faded(level, elapsedTicks, fadePerSecond) + Math.max(0.0, loudness);
  }

  public static boolean carries(final double distanceSquared, final int reach) {
    return distanceSquared <= (double) reach * reach;
  }

  public static boolean wakes(final double level, final int threshold) {
    return level >= threshold;
  }
}
