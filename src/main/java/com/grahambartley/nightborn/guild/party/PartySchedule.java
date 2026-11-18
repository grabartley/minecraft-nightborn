package com.grahambartley.nightborn.guild.party;

public final class PartySchedule {

  public enum Step {
    PITCH,
    SLEEP,
    WAKE,
    STRIKE,
    NONE
  }

  public static final long DAY = 24000L;
  public static final long DUSK = 11800L;
  public static final long NIGHT = 13000L;
  public static final long DAWN = 23000L;

  private PartySchedule() {}

  public static long day(final long timeOfDay) {
    return Math.floorDiv(timeOfDay, DAY);
  }

  public static boolean night(final long timeOfDay) {
    final long time = Math.floorMod(timeOfDay, DAY);
    return time >= NIGHT && time < DAWN;
  }

  public static boolean dusk(final long timeOfDay) {
    final long time = Math.floorMod(timeOfDay, DAY);
    return time >= DUSK && time < DAWN;
  }

  public static Step step(
      final long timeOfDay,
      final boolean pitched,
      final long campDay,
      final int nights,
      final long lastTriedDay) {
    final long day = day(timeOfDay);
    if (pitched) {
      if (day - campDay >= nights) {
        return Step.STRIKE;
      }
      return night(timeOfDay) ? Step.SLEEP : Step.WAKE;
    }
    return dusk(timeOfDay) && lastTriedDay != day ? Step.PITCH : Step.NONE;
  }

  public static boolean leaves(
      final boolean pitched, final long now, final long lastPlayerNear, final long leaveTicks) {
    return !pitched && now - lastPlayerNear >= leaveTicks;
  }
}
