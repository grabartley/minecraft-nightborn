package com.grahambartley.nightborn.config;

import java.util.List;

public record BloodSettings(
    int restingDrainSeconds,
    int animalBlood,
    int villagerBlood,
    int playerBlood,
    int bottledBlood) {

  static final String SECTION = "blood";

  public static final IntSetting RESTING_DRAIN_SECONDS =
      new IntSetting(
          SECTION, "restingDrainSeconds", 30, 5, 600, c -> c.blood().restingDrainSeconds());
  public static final IntSetting ANIMAL_BLOOD =
      new IntSetting(SECTION, "animalBlood", 50, 0, 500, c -> c.blood().animalBlood());
  public static final IntSetting VILLAGER_BLOOD =
      new IntSetting(SECTION, "villagerBlood", 100, 0, 500, c -> c.blood().villagerBlood());
  public static final IntSetting PLAYER_BLOOD =
      new IntSetting(SECTION, "playerBlood", 150, 0, 500, c -> c.blood().playerBlood());
  public static final IntSetting BOTTLED_BLOOD =
      new IntSetting(SECTION, "bottledBlood", 60, 0, 99, c -> c.blood().bottledBlood());

  public static final List<Setting<?>> SETTINGS =
      List.of(RESTING_DRAIN_SECONDS, ANIMAL_BLOOD, VILLAGER_BLOOD, PLAYER_BLOOD, BOTTLED_BLOOD);

  public BloodSettings {
    restingDrainSeconds = RESTING_DRAIN_SECONDS.clamp(restingDrainSeconds);
    animalBlood = ANIMAL_BLOOD.clamp(animalBlood);
    villagerBlood = VILLAGER_BLOOD.clamp(villagerBlood);
    playerBlood = PLAYER_BLOOD.clamp(playerBlood);
    bottledBlood = BOTTLED_BLOOD.clamp(bottledBlood);
  }

  static BloodSettings from(final SettingValues values) {
    return new BloodSettings(
        values.get(RESTING_DRAIN_SECONDS),
        values.get(ANIMAL_BLOOD),
        values.get(VILLAGER_BLOOD),
        values.get(PLAYER_BLOOD),
        values.get(BOTTLED_BLOOD));
  }
}
