package com.grahambartley.nightborn.config;

import java.util.List;

public record MobSettings(int vampireSpawnChance, int vampireBatSpawnChance, int batEscortChance) {

  static final String SECTION = "mobs";

  public static final IntSetting VAMPIRE_SPAWN_CHANCE =
      new IntSetting(
          SECTION, "vampireSpawnChance", 100, 0, 100, c -> c.mobs().vampireSpawnChance());
  public static final IntSetting VAMPIRE_BAT_SPAWN_CHANCE =
      new IntSetting(
          SECTION, "vampireBatSpawnChance", 100, 0, 100, c -> c.mobs().vampireBatSpawnChance());
  public static final IntSetting BAT_ESCORT_CHANCE =
      new IntSetting(SECTION, "batEscortChance", 40, 0, 100, c -> c.mobs().batEscortChance());

  public static final List<Setting<?>> SETTINGS =
      List.of(VAMPIRE_SPAWN_CHANCE, VAMPIRE_BAT_SPAWN_CHANCE, BAT_ESCORT_CHANCE);

  public MobSettings {
    vampireSpawnChance = VAMPIRE_SPAWN_CHANCE.clamp(vampireSpawnChance);
    vampireBatSpawnChance = VAMPIRE_BAT_SPAWN_CHANCE.clamp(vampireBatSpawnChance);
    batEscortChance = BAT_ESCORT_CHANCE.clamp(batEscortChance);
  }

  static MobSettings from(final SettingValues values) {
    return new MobSettings(
        values.get(VAMPIRE_SPAWN_CHANCE),
        values.get(VAMPIRE_BAT_SPAWN_CHANCE),
        values.get(BAT_ESCORT_CHANCE));
  }
}
