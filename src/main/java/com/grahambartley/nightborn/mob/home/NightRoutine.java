package com.grahambartley.nightborn.mob.home;

import com.grahambartley.nightborn.core.NightTime;
import com.grahambartley.nightborn.mob.ShadeRules;

public final class NightRoutine {

  public static final int HOME_RADIUS = 16;
  public static final long NO_NIGHT = Long.MIN_VALUE;
  static final double BLOCKS_PER_TICK_PER_SPEED = 0.25;
  static final double PATH_MARGIN = 2.0;
  static final long SETTLE_TICKS = 600L;

  private NightRoutine() {}

  public static long night(final long timeOfDay) {
    return Math.floorDiv(timeOfDay - NightTime.DUSK, NightTime.DAY_LENGTH);
  }

  public static boolean bedtime(final boolean fixedTime, final long timeOfDay) {
    return ShadeRules.sunIsNear(fixedTime, timeOfDay);
  }

  public static boolean headsHome(
      final boolean fixedTime,
      final long timeOfDay,
      final double distanceHome,
      final double movementSpeed) {
    if (fixedTime) {
      return false;
    }
    if (bedtime(false, timeOfDay)) {
      return true;
    }
    final double blocksPerTick = Math.max(0.01, movementSpeed * BLOCKS_PER_TICK_PER_SPEED);
    return ShadeRules.ticksUntilSunIsNear(timeOfDay)
        <= distanceHome / blocksPerTick * PATH_MARGIN + SETTLE_TICKS;
  }

  public static boolean goesOut(final boolean hungry, final int roll, final int chance) {
    return hungry && roll < chance;
  }

  public static boolean out(
      final long outingNight, final long night, final boolean hungry, final boolean headsHome) {
    return outingNight != NO_NIGHT && outingNight == night && hungry && !headsHome;
  }

  public static int roamRadius(final boolean out, final int feedingRange) {
    return out ? Math.max(HOME_RADIUS, feedingRange) : HOME_RADIUS;
  }
}
