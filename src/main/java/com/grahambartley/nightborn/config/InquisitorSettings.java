package com.grahambartley.nightborn.config;

import java.util.List;

public record InquisitorSettings(
    int experience,
    int healthPerVampire,
    int arenaRadius,
    int drainWindowSeconds,
    int drainSeconds,
    int riseHealth,
    int deathNotorietyLoss,
    int emptyArenaSeconds) {

  static final String SECTION = HuntSettings.SECTION;

  public static final IntSetting EXPERIENCE =
      new IntSetting(
          SECTION, "inquisitorExperience", 4000, 0, 50000, c -> c.inquisitor().experience());
  public static final IntSetting HEALTH_PER_VAMPIRE =
      new IntSetting(
          SECTION, "healthPerVampire", 50, 0, 500, c -> c.inquisitor().healthPerVampire());
  public static final IntSetting ARENA_RADIUS =
      new IntSetting(SECTION, "arenaRadius", 14, 8, 48, c -> c.inquisitor().arenaRadius());
  public static final IntSetting DRAIN_WINDOW_SECONDS =
      new IntSetting(
          SECTION, "drainWindowSeconds", 20, 5, 120, c -> c.inquisitor().drainWindowSeconds());
  public static final IntSetting DRAIN_SECONDS =
      new IntSetting(SECTION, "drainSeconds", 5, 2, 30, c -> c.inquisitor().drainSeconds());
  public static final IntSetting RISE_HEALTH =
      new IntSetting(SECTION, "riseHealth", 30, 1, 100, c -> c.inquisitor().riseHealth());
  public static final IntSetting DEATH_NOTORIETY_LOSS =
      new IntSetting(
          SECTION, "deathNotorietyLoss", 50, 0, 100, c -> c.inquisitor().deathNotorietyLoss());
  public static final IntSetting EMPTY_ARENA_SECONDS =
      new IntSetting(
          SECTION, "emptyArenaSeconds", 30, 5, 600, c -> c.inquisitor().emptyArenaSeconds());

  public static final List<Setting<?>> SETTINGS =
      List.of(
          EXPERIENCE,
          HEALTH_PER_VAMPIRE,
          ARENA_RADIUS,
          DRAIN_WINDOW_SECONDS,
          DRAIN_SECONDS,
          RISE_HEALTH,
          DEATH_NOTORIETY_LOSS,
          EMPTY_ARENA_SECONDS);

  public InquisitorSettings {
    experience = EXPERIENCE.clamp(experience);
    healthPerVampire = HEALTH_PER_VAMPIRE.clamp(healthPerVampire);
    arenaRadius = ARENA_RADIUS.clamp(arenaRadius);
    drainWindowSeconds = DRAIN_WINDOW_SECONDS.clamp(drainWindowSeconds);
    drainSeconds = DRAIN_SECONDS.clamp(drainSeconds);
    riseHealth = RISE_HEALTH.clamp(riseHealth);
    deathNotorietyLoss = DEATH_NOTORIETY_LOSS.clamp(deathNotorietyLoss);
    emptyArenaSeconds = EMPTY_ARENA_SECONDS.clamp(emptyArenaSeconds);
  }

  static InquisitorSettings from(final SettingValues values) {
    return new InquisitorSettings(
        values.get(EXPERIENCE),
        values.get(HEALTH_PER_VAMPIRE),
        values.get(ARENA_RADIUS),
        values.get(DRAIN_WINDOW_SECONDS),
        values.get(DRAIN_SECONDS),
        values.get(RISE_HEALTH),
        values.get(DEATH_NOTORIETY_LOSS),
        values.get(EMPTY_ARENA_SECONDS));
  }
}
