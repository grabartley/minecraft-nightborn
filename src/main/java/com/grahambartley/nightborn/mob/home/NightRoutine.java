package com.grahambartley.nightborn.mob.home;

import com.grahambartley.nightborn.core.NightTime;
import com.grahambartley.nightborn.mob.ShadeRules;

public final class NightRoutine {

  public static final long NO_NIGHT = Long.MIN_VALUE;
  static final double BLOCKS_PER_TICK_PER_SPEED = 0.25;
  static final double PATH_MARGIN = 2.0;
  static final long SETTLE_TICKS = 600L;
  static final int COFFIN_REACH_HOMES = 2;

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

  public static boolean decidesTonight(
      final boolean bedtime, final long decidedNight, final long night, final boolean hungry) {
    return !bedtime && hungry && decidedNight != night;
  }

  public static boolean goesOut(final int roll, final int chance) {
    return roll < chance;
  }

  public static boolean out(
      final long outingNight, final long night, final boolean hungry, final boolean headsHome) {
    return outingNight != NO_NIGHT && outingNight == night && hungry && !headsHome;
  }

  public static int roamRadius(final boolean out, final int feedingRange, final int homeRadius) {
    return out ? Math.max(homeRadius, feedingRange) : homeRadius;
  }

  public static boolean outOfRange(
      final boolean provoked,
      final boolean out,
      final double vampireDistanceSquared,
      final double targetDistanceSquared,
      final int chaseRange,
      final int feedingRange,
      final int homeRadius) {
    if (provoked) {
      final double reach = out ? Math.max(chaseRange, feedingRange) : chaseRange;
      return vampireDistanceSquared > reach * reach;
    }
    final double reach = roamRadius(out, feedingRange, homeRadius);
    return targetDistanceSquared > reach * reach;
  }

  public static boolean readyForBed(
      final boolean bedtime, final boolean hasTarget, final boolean downed) {
    return bedtime && !hasTarget && !downed;
  }

  public static boolean walksToCoffin(
      final boolean known,
      final double distanceSquared,
      final int homeRadius,
      final boolean loaded,
      final boolean free) {
    final double reach = (double) homeRadius * COFFIN_REACH_HOMES;
    return known && distanceSquared <= reach * reach && loaded && free;
  }
}
