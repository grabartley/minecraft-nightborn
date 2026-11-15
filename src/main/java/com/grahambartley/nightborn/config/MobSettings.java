package com.grahambartley.nightborn.config;

import java.util.List;

public record MobSettings(
    int vampireSpawnChance,
    int vampireBatSpawnChance,
    int batEscortChance,
    int vampireLevitateSeconds,
    int vampireLandSeconds) {

  static final String SECTION = "mobs";

  public static final IntSetting VAMPIRE_SPAWN_CHANCE =
      new IntSetting(
          SECTION, "vampireSpawnChance", 100, 0, 100, c -> c.mobs().vampireSpawnChance());
  public static final IntSetting VAMPIRE_BAT_SPAWN_CHANCE =
      new IntSetting(
          SECTION, "vampireBatSpawnChance", 100, 0, 100, c -> c.mobs().vampireBatSpawnChance());
  public static final IntSetting BAT_ESCORT_CHANCE =
      new IntSetting(SECTION, "batEscortChance", 40, 0, 100, c -> c.mobs().batEscortChance());

  public static final IntSetting VAMPIRE_LEVITATE_SECONDS =
      new IntSetting(
          SECTION, "vampireLevitateSeconds", 30, 0, 3600, c -> c.mobs().vampireLevitateSeconds());
  public static final IntSetting VAMPIRE_LAND_SECONDS =
      new IntSetting(
          SECTION, "vampireLandSeconds", 15, 0, 3600, c -> c.mobs().vampireLandSeconds());

  public static final List<Setting<?>> SETTINGS =
      List.of(
          VAMPIRE_SPAWN_CHANCE,
          VAMPIRE_BAT_SPAWN_CHANCE,
          BAT_ESCORT_CHANCE,
          VAMPIRE_LEVITATE_SECONDS,
          VAMPIRE_LAND_SECONDS);

  public MobSettings {
    vampireSpawnChance = VAMPIRE_SPAWN_CHANCE.clamp(vampireSpawnChance);
    vampireBatSpawnChance = VAMPIRE_BAT_SPAWN_CHANCE.clamp(vampireBatSpawnChance);
    batEscortChance = BAT_ESCORT_CHANCE.clamp(batEscortChance);
    vampireLevitateSeconds = VAMPIRE_LEVITATE_SECONDS.clamp(vampireLevitateSeconds);
    vampireLandSeconds = VAMPIRE_LAND_SECONDS.clamp(vampireLandSeconds);
  }

  static MobSettings from(final SettingValues values) {
    return new MobSettings(
        values.get(VAMPIRE_SPAWN_CHANCE),
        values.get(VAMPIRE_BAT_SPAWN_CHANCE),
        values.get(BAT_ESCORT_CHANCE),
        values.get(VAMPIRE_LEVITATE_SECONDS),
        values.get(VAMPIRE_LAND_SECONDS));
  }
}
