package com.grahambartley.nightborn.mob;

import java.util.function.BooleanSupplier;

public final class MobSpawnRules {

  static final int MAX_ESCORT = 2;
  public static final int MAX_NEARBY_BATS = 2;
  public static final double NEARBY_RANGE = 128.0;
  public static final double NEARBY_HEIGHT = 64.0;

  private MobSpawnRules() {}

  public static boolean batMaySpawn(
      final int roll,
      final int chance,
      final BooleanSupplier vanillaBatCave,
      final BooleanSupplier inVampireStructure) {
    return roll < chance && (vanillaBatCave.getAsBoolean() || inVampireStructure.getAsBoolean());
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
