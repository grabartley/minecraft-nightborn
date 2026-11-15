package com.grahambartley.nightborn.config;

import java.util.List;

public record StructureSettings(int structureRarity) {

  static final String SECTION = "structures";

  public static final IntSetting STRUCTURE_RARITY =
      new IntSetting(
          SECTION, "structureRarity", 100, 25, 400, c -> c.structures().structureRarity());

  public static final List<Setting<?>> SETTINGS = List.of(STRUCTURE_RARITY);

  public StructureSettings {
    structureRarity = STRUCTURE_RARITY.clamp(structureRarity);
  }

  static StructureSettings from(final SettingValues values) {
    return new StructureSettings(values.get(STRUCTURE_RARITY));
  }
}
