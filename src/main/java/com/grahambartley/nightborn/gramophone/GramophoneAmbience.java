package com.grahambartley.nightborn.gramophone;

public final class GramophoneAmbience {

  public static final int UNSET = -1;
  public static final int CHECK_TICKS = 20;

  private GramophoneAmbience() {}

  public static int firstWait(final int intervalSeconds, final double roll) {
    return Math.max(1, (int) Math.round(intervalSeconds * (0.5 + roll)));
  }

  public static int step(
      final int wait, final boolean playerNear, final int intervalSeconds, final double roll) {
    if (!playerNear) {
      return wait;
    }
    if (wait == UNSET) {
      return firstWait(intervalSeconds, roll);
    }
    return Math.max(0, wait - 1);
  }

  public static boolean due(final int wait) {
    return wait == 0;
  }
}
