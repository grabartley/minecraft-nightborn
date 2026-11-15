package com.grahambartley.nightborn.config;

import java.util.List;

public record BloodSettings(
    int restingDrainSeconds,
    int animalBlood,
    int villagerBlood,
    int playerBlood,
    int bottledBlood,
    int starvingStepSeconds,
    int starvingStep,
    int starvingHurtDelaySeconds,
    int starvingHurtSeconds,
    int vomitDamage) {

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
  public static final IntSetting STARVING_STEP_SECONDS =
      new IntSetting(
          SECTION, "starvingStepSeconds", 60, 5, 1200, c -> c.blood().starvingStepSeconds());
  public static final IntSetting STARVING_STEP =
      new IntSetting(SECTION, "starvingStep", 10, 1, 100, c -> c.blood().starvingStep());
  public static final IntSetting STARVING_HURT_DELAY_SECONDS =
      new IntSetting(
          SECTION,
          "starvingHurtDelaySeconds",
          300,
          0,
          3600,
          c -> c.blood().starvingHurtDelaySeconds());
  public static final IntSetting STARVING_HURT_SECONDS =
      new IntSetting(
          SECTION, "starvingHurtSeconds", 10, 1, 600, c -> c.blood().starvingHurtSeconds());
  public static final IntSetting VOMIT_DAMAGE =
      new IntSetting(SECTION, "vomitDamage", 4, 0, 40, c -> c.blood().vomitDamage());

  public static final List<Setting<?>> SETTINGS =
      List.of(
          RESTING_DRAIN_SECONDS,
          ANIMAL_BLOOD,
          VILLAGER_BLOOD,
          PLAYER_BLOOD,
          BOTTLED_BLOOD,
          STARVING_STEP_SECONDS,
          STARVING_STEP,
          STARVING_HURT_DELAY_SECONDS,
          STARVING_HURT_SECONDS,
          VOMIT_DAMAGE);

  public BloodSettings {
    restingDrainSeconds = RESTING_DRAIN_SECONDS.clamp(restingDrainSeconds);
    animalBlood = ANIMAL_BLOOD.clamp(animalBlood);
    villagerBlood = VILLAGER_BLOOD.clamp(villagerBlood);
    playerBlood = PLAYER_BLOOD.clamp(playerBlood);
    bottledBlood = BOTTLED_BLOOD.clamp(bottledBlood);
    starvingStepSeconds = STARVING_STEP_SECONDS.clamp(starvingStepSeconds);
    starvingStep = STARVING_STEP.clamp(starvingStep);
    starvingHurtDelaySeconds = STARVING_HURT_DELAY_SECONDS.clamp(starvingHurtDelaySeconds);
    starvingHurtSeconds = STARVING_HURT_SECONDS.clamp(starvingHurtSeconds);
    vomitDamage = VOMIT_DAMAGE.clamp(vomitDamage);
  }

  static BloodSettings from(final SettingValues values) {
    return new BloodSettings(
        values.get(RESTING_DRAIN_SECONDS),
        values.get(ANIMAL_BLOOD),
        values.get(VILLAGER_BLOOD),
        values.get(PLAYER_BLOOD),
        values.get(BOTTLED_BLOOD),
        values.get(STARVING_STEP_SECONDS),
        values.get(STARVING_STEP),
        values.get(STARVING_HURT_DELAY_SECONDS),
        values.get(STARVING_HURT_SECONDS),
        values.get(VOMIT_DAMAGE));
  }
}
