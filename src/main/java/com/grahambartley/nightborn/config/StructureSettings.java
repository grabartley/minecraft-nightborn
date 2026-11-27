package com.grahambartley.nightborn.config;

import java.util.List;

public record StructureSettings(
    int structureRarity,
    int campRarity,
    int gramophoneSeconds,
    int gramophoneRange,
    int rumourRange,
    int refillMinDays,
    int refillMaxDays,
    int wildVampireChance,
    int wildVampireDistance,
    int wildVampireHungryChance) {

  static final String SECTION = "structures";

  public static final IntSetting STRUCTURE_RARITY =
      new IntSetting(
          SECTION, "structureRarity", 100, 25, 400, c -> c.structures().structureRarity());
  public static final IntSetting CAMP_RARITY =
      new IntSetting(SECTION, "campRarity", 100, 25, 400, c -> c.structures().campRarity());
  public static final IntSetting GRAMOPHONE_SECONDS =
      new IntSetting(
          SECTION, "gramophoneSeconds", 300, 20, 3600, c -> c.structures().gramophoneSeconds());
  public static final IntSetting GRAMOPHONE_RANGE =
      new IntSetting(SECTION, "gramophoneRange", 24, 8, 64, c -> c.structures().gramophoneRange());
  public static final IntSetting RUMOUR_RANGE =
      new IntSetting(SECTION, "rumourRange", 1024, 128, 4096, c -> c.structures().rumourRange());

  public static final IntSetting REFILL_MIN_DAYS =
      new IntSetting(SECTION, "refillMinDays", 15, 1, 365, c -> c.structures().refillMinDays());
  public static final IntSetting REFILL_MAX_DAYS =
      new IntSetting(SECTION, "refillMaxDays", 40, 1, 365, c -> c.structures().refillMaxDays());
  public static final IntSetting WILD_VAMPIRE_CHANCE =
      new IntSetting(
          SECTION, "wildVampireChance", 2, 0, 100, c -> c.structures().wildVampireChance());
  public static final IntSetting WILD_VAMPIRE_DISTANCE =
      new IntSetting(
          SECTION, "wildVampireDistance", 128, 64, 512, c -> c.structures().wildVampireDistance());
  public static final IntSetting WILD_VAMPIRE_HUNGRY_CHANCE =
      new IntSetting(
          SECTION,
          "wildVampireHungryChance",
          50,
          0,
          100,
          c -> c.structures().wildVampireHungryChance());

  public static final List<Setting<?>> SETTINGS =
      List.of(
          STRUCTURE_RARITY,
          CAMP_RARITY,
          GRAMOPHONE_SECONDS,
          GRAMOPHONE_RANGE,
          RUMOUR_RANGE,
          REFILL_MIN_DAYS,
          REFILL_MAX_DAYS,
          WILD_VAMPIRE_CHANCE,
          WILD_VAMPIRE_DISTANCE,
          WILD_VAMPIRE_HUNGRY_CHANCE);

  public StructureSettings {
    structureRarity = STRUCTURE_RARITY.clamp(structureRarity);
    campRarity = CAMP_RARITY.clamp(campRarity);
    gramophoneSeconds = GRAMOPHONE_SECONDS.clamp(gramophoneSeconds);
    gramophoneRange = GRAMOPHONE_RANGE.clamp(gramophoneRange);
    rumourRange = RUMOUR_RANGE.clamp(rumourRange);
    refillMinDays = REFILL_MIN_DAYS.clamp(refillMinDays);
    refillMaxDays = Math.max(refillMinDays, REFILL_MAX_DAYS.clamp(refillMaxDays));
    wildVampireChance = WILD_VAMPIRE_CHANCE.clamp(wildVampireChance);
    wildVampireDistance = WILD_VAMPIRE_DISTANCE.clamp(wildVampireDistance);
    wildVampireHungryChance = WILD_VAMPIRE_HUNGRY_CHANCE.clamp(wildVampireHungryChance);
  }

  static StructureSettings from(final SettingValues values) {
    return new StructureSettings(
        values.get(STRUCTURE_RARITY),
        values.get(CAMP_RARITY),
        values.get(GRAMOPHONE_SECONDS),
        values.get(GRAMOPHONE_RANGE),
        values.get(RUMOUR_RANGE),
        values.get(REFILL_MIN_DAYS),
        values.get(REFILL_MAX_DAYS),
        values.get(WILD_VAMPIRE_CHANCE),
        values.get(WILD_VAMPIRE_DISTANCE),
        values.get(WILD_VAMPIRE_HUNGRY_CHANCE));
  }
}
