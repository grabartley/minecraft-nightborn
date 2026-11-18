package com.grahambartley.nightborn.structure;

import com.grahambartley.nightborn.core.NightTime;
import java.util.function.IntUnaryOperator;

public final class RefillClock {

  private RefillClock() {}

  public static long drawn(
      final long now, final int minDays, final int maxDays, final IntUnaryOperator nextInt) {
    return now
        + (minDays + (long) nextInt.applyAsInt(maxDays - minDays + 1)) * NightTime.DAY_LENGTH;
  }

  public enum Step {
    WAIT,
    DRAW,
    CLEAR,
    ARRIVE
  }

  public static Step step(
      final HouseholdRecord record, final long now, final int maxDays, final boolean claimed) {
    if (claimed) {
      return record.refillAt().isPresent() ? Step.CLEAR : Step.WAIT;
    }
    if (redraws(record, now, maxDays)) {
      return Step.DRAW;
    }
    return record.refillDue(now) ? Step.ARRIVE : Step.WAIT;
  }

  static boolean redraws(final HouseholdRecord record, final long now, final int maxDays) {
    return record.awaitsRefillTime()
        || record.gone()
            && record
                .refillAt()
                .filter(at -> at - now > (long) maxDays * NightTime.DAY_LENGTH)
                .isPresent();
  }

  public static boolean mayArrive(
      final boolean known, final HouseholdRecord record, final boolean claimed) {
    return known && record.gone() && !claimed;
  }
}
