package com.grahambartley.nightborn.oldblood;

import com.grahambartley.nightborn.sickness.SicknessStage;

public final class FontRefill {

  public static final long FULL = -1L;
  static final int HOUSEHOLD_PACE = 2;
  static final int EMPTY_PACE = 1;

  private FontRefill() {}

  public static long needed(final int refillDays) {
    return (long) Math.max(1, refillDays) * SicknessStage.TICKS_PER_DAY * HOUSEHOLD_PACE;
  }

  public static boolean full(final long progress) {
    return progress == FULL;
  }

  public static long advance(
      final long progress, final long elapsed, final boolean household, final int refillDays) {
    if (full(progress)) {
      return FULL;
    }
    final long next =
        Math.max(0L, progress) + Math.max(0L, elapsed) * (household ? HOUSEHOLD_PACE : EMPTY_PACE);
    return next >= needed(refillDays) ? FULL : next;
  }
}
