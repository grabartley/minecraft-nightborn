package com.grahambartley.nightborn.challenge;

import com.grahambartley.nightborn.core.NightTime;
import java.util.OptionalLong;

public final class ChallengeClock {

  private ChallengeClock() {}

  public static long period(final long timeOfDay, final int daysPerChange) {
    return Math.floorDiv(
        Math.floorDiv(timeOfDay, NightTime.DAY_LENGTH), Math.max(1, daysPerChange));
  }

  public static OptionalLong night(final long timeOfDay) {
    return NightTime.isNight(false, timeOfDay)
        ? OptionalLong.of(NightTime.night(timeOfDay))
        : OptionalLong.empty();
  }
}
