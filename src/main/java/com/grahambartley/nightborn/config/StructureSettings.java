package com.grahambartley.nightborn.config;

import java.util.List;

public record StructureSettings(
    int structureRarity, int campRarity, int gramophoneSeconds, int gramophoneRange) {

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

  public static final List<Setting<?>> SETTINGS =
      List.of(STRUCTURE_RARITY, CAMP_RARITY, GRAMOPHONE_SECONDS, GRAMOPHONE_RANGE);

  public StructureSettings {
    structureRarity = STRUCTURE_RARITY.clamp(structureRarity);
    campRarity = CAMP_RARITY.clamp(campRarity);
    gramophoneSeconds = GRAMOPHONE_SECONDS.clamp(gramophoneSeconds);
    gramophoneRange = GRAMOPHONE_RANGE.clamp(gramophoneRange);
  }

  static StructureSettings from(final SettingValues values) {
    return new StructureSettings(
        values.get(STRUCTURE_RARITY),
        values.get(CAMP_RARITY),
        values.get(GRAMOPHONE_SECONDS),
        values.get(GRAMOPHONE_RANGE));
  }
}
