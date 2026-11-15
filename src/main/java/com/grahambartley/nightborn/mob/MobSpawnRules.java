package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.core.NightTime;

public final class MobSpawnRules {

  static final int DARK_CAVE_LIGHT = 4;
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
      final boolean fixedTime,
      final long timeOfDay,
      final boolean openSky,
      final int light,
      final int roll,
      final int chance) {
    final boolean darkCave = !openSky && light <= DARK_CAVE_LIGHT;
    final boolean openNight = openSky && NightTime.isNight(fixedTime, timeOfDay);
    return (darkCave || openNight) && roll < chance;
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
