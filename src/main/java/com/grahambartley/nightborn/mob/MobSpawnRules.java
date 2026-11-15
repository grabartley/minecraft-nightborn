package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.core.NightTime;
import java.util.function.BooleanSupplier;

public final class MobSpawnRules {

  public static final int CAVE_DARKNESS = 4;
  static final int MAX_ESCORT = 2;
  public static final int MAX_NEARBY_BATS = 2;
  public static final double NEARBY_RANGE = 128.0;
  public static final double NEARBY_HEIGHT = 64.0;

  private MobSpawnRules() {}

  public static boolean vampireMaySpawn(
      final boolean fixedTime,
      final long timeOfDay,
      final boolean openSky,
      final int roll,
      final int chance) {
    return openSky && NightTime.isNight(fixedTime, timeOfDay) && roll < chance;
  }

  public static boolean batMaySpawn(
      final boolean belowSeaLevel,
      final int light,
      final int darknessRoll,
      final BooleanSupplier inVampireStructure,
      final int roll,
      final int chance) {
    if (roll >= chance) {
      return false;
    }
    return (belowSeaLevel && light <= darknessRoll) || inVampireStructure.getAsBoolean();
  }

  public static boolean crowded(final int nearbyBats) {
    return nearbyBats >= MAX_NEARBY_BATS;
  }

  public static int escortSize(final int roll, final int chance, final int extraBats) {
    if (roll >= chance) {
      return 0;
    }
    return Math.min(MAX_ESCORT, 1 + Math.max(0, extraBats));
  }
}
