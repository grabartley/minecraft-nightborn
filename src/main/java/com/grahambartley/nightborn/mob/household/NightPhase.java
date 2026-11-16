package com.grahambartley.nightborn.mob.household;

import com.grahambartley.nightborn.core.NightTime;

public enum NightPhase {
  DAY,
  GATHER,
  PASTIME,
  WIND_DOWN;

  static final long GATHER_TICKS = 1500L;
  static final long WIND_DOWN_START = NightTime.DAWN - 2000L;

  public static NightPhase of(final boolean fixedTime, final long timeOfDay) {
    if (fixedTime) {
      return PASTIME;
    }
    if (!NightTime.isNight(false, timeOfDay)) {
      return DAY;
    }
    final long time = Math.floorMod(timeOfDay, NightTime.DAY_LENGTH);
    if (time < NightTime.DUSK + GATHER_TICKS) {
      return GATHER;
    }
    return time >= WIND_DOWN_START ? WIND_DOWN : PASTIME;
  }

  public boolean lightsWanted() {
    return this == GATHER || this == PASTIME;
  }

  public boolean choresDue() {
    return this == GATHER || this == WIND_DOWN;
  }
}
