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
    int emptyArenaSeconds,
    int greatswordBlood,
    int setBlood,
    int setExtraBlood,
    int setDrainSlower) {

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

  public static final IntSetting GREATSWORD_BLOOD =
      new IntSetting(SECTION, "greatswordBlood", 10, 0, 100, c -> c.inquisitor().greatswordBlood());
  public static final IntSetting SET_BLOOD =
      new IntSetting(SECTION, "setBlood", 20, 0, 100, c -> c.inquisitor().setBlood());
  public static final IntSetting SET_EXTRA_BLOOD =
      new IntSetting(SECTION, "setExtraBlood", 4, 0, 20, c -> c.inquisitor().setExtraBlood());
  public static final IntSetting SET_DRAIN_SLOWER =
      new IntSetting(SECTION, "setDrainSlower", 25, 0, 80, c -> c.inquisitor().setDrainSlower());

  public static final List<Setting<?>> SETTINGS =
      List.of(
          EXPERIENCE,
          HEALTH_PER_VAMPIRE,
          ARENA_RADIUS,
          DRAIN_WINDOW_SECONDS,
          DRAIN_SECONDS,
          RISE_HEALTH,
          DEATH_NOTORIETY_LOSS,
          EMPTY_ARENA_SECONDS,
          GREATSWORD_BLOOD,
          SET_BLOOD,
          SET_EXTRA_BLOOD,
          SET_DRAIN_SLOWER);

  public InquisitorSettings {
    experience = EXPERIENCE.clamp(experience);
    healthPerVampire = HEALTH_PER_VAMPIRE.clamp(healthPerVampire);
    arenaRadius = ARENA_RADIUS.clamp(arenaRadius);
    drainWindowSeconds = DRAIN_WINDOW_SECONDS.clamp(drainWindowSeconds);
    drainSeconds = DRAIN_SECONDS.clamp(drainSeconds);
    riseHealth = RISE_HEALTH.clamp(riseHealth);
    deathNotorietyLoss = DEATH_NOTORIETY_LOSS.clamp(deathNotorietyLoss);
    emptyArenaSeconds = EMPTY_ARENA_SECONDS.clamp(emptyArenaSeconds);
    greatswordBlood = GREATSWORD_BLOOD.clamp(greatswordBlood);
    setBlood = SET_BLOOD.clamp(setBlood);
    setExtraBlood = SET_EXTRA_BLOOD.clamp(setExtraBlood);
    setDrainSlower = SET_DRAIN_SLOWER.clamp(setDrainSlower);
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
        values.get(EMPTY_ARENA_SECONDS),
        values.get(GREATSWORD_BLOOD),
        values.get(SET_BLOOD),
        values.get(SET_EXTRA_BLOOD),
        values.get(SET_DRAIN_SLOWER));
  }
}
