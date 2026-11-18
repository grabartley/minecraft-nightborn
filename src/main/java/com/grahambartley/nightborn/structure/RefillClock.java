package com.grahambartley.nightborn.structure;

import com.grahambartley.nightborn.core.NightTime;
import java.util.function.IntUnaryOperator;

public final class RefillClock {

  private RefillClock() {}

  public static long drawn(
      final long now, final int minDays, final int maxDays, final IntUnaryOperator nextInt) {
    final int low = Math.min(minDays, maxDays);
    final int high = Math.max(minDays, maxDays);
    return now + (low + (long) nextInt.applyAsInt(high - low + 1)) * NightTime.DAY_LENGTH;
  }

  public static boolean mayArrive(
      final boolean known, final HouseholdRecord record, final boolean claimed) {
    return known && record.gone() && !claimed;
  }
}
