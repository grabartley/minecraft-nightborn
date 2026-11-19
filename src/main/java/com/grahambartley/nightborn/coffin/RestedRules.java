package com.grahambartley.nightborn.coffin;

import com.grahambartley.nightborn.core.NightTime;
import com.grahambartley.nightborn.house.House;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;

public final class RestedRules {

  public static final int RESTED = 0;
  public static final int RESTED_AT_HOME = 1;

  private RestedRules() {}

  public static boolean earned(
      final boolean vampirePlayer,
      final boolean inCoffin,
      final boolean fullyAsleep,
      final boolean night,
      final boolean wholeDay) {
    return vampirePlayer && inCoffin && fullyAsleep && night && wholeDay;
  }

  public static boolean wholeDay(
      final boolean skipped,
      final OptionalLong sleptFrom,
      final long timeOfDay,
      final int windowSeconds) {
    return skipped
        || sleptFrom.isPresent()
            && sleptFrom.getAsLong() <= dawnBefore(timeOfDay) + windowSeconds * 20L;
  }

  public static long dawnBefore(final long timeOfDay) {
    return timeOfDay - Math.floorMod(timeOfDay - NightTime.DAWN, NightTime.DAY_LENGTH);
  }

  public static int ticksUntilDawn(final long timeOfDay) {
    return (int) Math.floorMod(NightTime.DAWN - timeOfDay, NightTime.DAY_LENGTH);
  }

  public static int level(final Optional<House> house, final Optional<UUID> coffinOwner) {
    return house.isPresent() && coffinOwner.filter(house.get()::belongs).isPresent()
        ? RESTED_AT_HOME
        : RESTED;
  }
}
