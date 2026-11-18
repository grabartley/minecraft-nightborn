package com.grahambartley.nightborn.coffin;

import com.grahambartley.nightborn.core.NightTime;
import com.grahambartley.nightborn.house.House;
import java.util.Optional;
import java.util.UUID;

public final class RestedRules {

  public static final int RESTED = 0;
  public static final int RESTED_AT_HOME = 1;

  private RestedRules() {}

  public static boolean earned(
      final boolean vampirePlayer,
      final boolean inCoffin,
      final boolean fullyAsleep,
      final boolean night) {
    return vampirePlayer && inCoffin && fullyAsleep && night;
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
