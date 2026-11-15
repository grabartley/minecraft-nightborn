package com.grahambartley.nightborn.core;

public final class NightTime {

  static final long DUSK = 13000L;
  static final long DAWN = 23000L;
  static final long DAY_LENGTH = 24000L;

  private NightTime() {}

  public static boolean isNight(final boolean fixedTime, final long timeOfDay) {
    if (fixedTime) {
      return false;
    }
    final long time = Math.floorMod(timeOfDay, DAY_LENGTH);
    return time >= DUSK && time < DAWN;
  }
}
