package com.grahambartley.nightborn.config;

import java.util.List;

public record StructureSettings(int structureRarity, int campRarity) {

  static final String SECTION = "structures";

  public static final IntSetting STRUCTURE_RARITY =
      new IntSetting(
          SECTION, "structureRarity", 100, 25, 400, c -> c.structures().structureRarity());
  public static final IntSetting CAMP_RARITY =
      new IntSetting(SECTION, "campRarity", 100, 25, 400, c -> c.structures().campRarity());

  public static final List<Setting<?>> SETTINGS = List.of(STRUCTURE_RARITY, CAMP_RARITY);

  public StructureSettings {
    structureRarity = STRUCTURE_RARITY.clamp(structureRarity);
    campRarity = CAMP_RARITY.clamp(campRarity);
  }

  static StructureSettings from(final SettingValues values) {
    return new StructureSettings(values.get(STRUCTURE_RARITY), values.get(CAMP_RARITY));
  }
}
