package com.grahambartley.nightborn.config;

import java.util.List;

public record MobSettings(
    int vampireBatSpawnChance,
    int batEscortChance,
    int vampireLevitateSeconds,
    int vampireLandSeconds,
    int vampireHissSeconds,
    int vampireHungerThreshold,
    int vampireBloodDrainSeconds,
    int vampireOutingChance,
    int vampireFeedingRange,
    int vampireChaseRange,
    int vampireHomeRadius,
    int vampireAlertRange,
    int eldestHealthPerPlayer,
    int eldestCrowdRange) {

  static final String SECTION = "mobs";

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

  public static final IntSetting VAMPIRE_HISS_SECONDS =
      new IntSetting(SECTION, "vampireHissSeconds", 8, 0, 600, c -> c.mobs().vampireHissSeconds());

  public static final IntSetting VAMPIRE_HUNGER_THRESHOLD =
      new IntSetting(
          SECTION, "vampireHungerThreshold", 14, 0, 20, c -> c.mobs().vampireHungerThreshold());
  public static final IntSetting VAMPIRE_BLOOD_DRAIN_SECONDS =
      new IntSetting(
          SECTION,
          "vampireBloodDrainSeconds",
          60,
          5,
          600,
          c -> c.mobs().vampireBloodDrainSeconds());
  public static final IntSetting VAMPIRE_OUTING_CHANCE =
      new IntSetting(
          SECTION, "vampireOutingChance", 50, 0, 100, c -> c.mobs().vampireOutingChance());
  public static final IntSetting VAMPIRE_FEEDING_RANGE =
      new IntSetting(
          SECTION, "vampireFeedingRange", 64, 16, 256, c -> c.mobs().vampireFeedingRange());
  public static final IntSetting VAMPIRE_CHASE_RANGE =
      new IntSetting(SECTION, "vampireChaseRange", 40, 16, 256, c -> c.mobs().vampireChaseRange());

  public static final IntSetting VAMPIRE_HOME_RADIUS =
      new IntSetting(SECTION, "vampireHomeRadius", 16, 8, 64, c -> c.mobs().vampireHomeRadius());
  public static final IntSetting VAMPIRE_ALERT_RANGE =
      new IntSetting(SECTION, "vampireAlertRange", 24, 4, 64, c -> c.mobs().vampireAlertRange());

  public static final IntSetting ELDEST_HEALTH_PER_PLAYER =
      new IntSetting(
          SECTION, "eldestHealthPerPlayer", 50, 0, 200, c -> c.mobs().eldestHealthPerPlayer());
  public static final IntSetting ELDEST_CROWD_RANGE =
      new IntSetting(SECTION, "eldestCrowdRange", 32, 8, 64, c -> c.mobs().eldestCrowdRange());

  public static final List<Setting<?>> SETTINGS =
      List.of(
          VAMPIRE_BAT_SPAWN_CHANCE,
          BAT_ESCORT_CHANCE,
          VAMPIRE_LEVITATE_SECONDS,
          VAMPIRE_LAND_SECONDS,
          VAMPIRE_HISS_SECONDS,
          VAMPIRE_HUNGER_THRESHOLD,
          VAMPIRE_BLOOD_DRAIN_SECONDS,
          VAMPIRE_OUTING_CHANCE,
          VAMPIRE_FEEDING_RANGE,
          VAMPIRE_CHASE_RANGE,
          VAMPIRE_HOME_RADIUS,
          VAMPIRE_ALERT_RANGE,
          ELDEST_HEALTH_PER_PLAYER,
          ELDEST_CROWD_RANGE);

  public MobSettings {
    vampireBatSpawnChance = VAMPIRE_BAT_SPAWN_CHANCE.clamp(vampireBatSpawnChance);
    batEscortChance = BAT_ESCORT_CHANCE.clamp(batEscortChance);
    vampireLevitateSeconds = VAMPIRE_LEVITATE_SECONDS.clamp(vampireLevitateSeconds);
    vampireLandSeconds = VAMPIRE_LAND_SECONDS.clamp(vampireLandSeconds);
    vampireHissSeconds = VAMPIRE_HISS_SECONDS.clamp(vampireHissSeconds);
    vampireHungerThreshold = VAMPIRE_HUNGER_THRESHOLD.clamp(vampireHungerThreshold);
    vampireBloodDrainSeconds = VAMPIRE_BLOOD_DRAIN_SECONDS.clamp(vampireBloodDrainSeconds);
    vampireOutingChance = VAMPIRE_OUTING_CHANCE.clamp(vampireOutingChance);
    vampireFeedingRange = VAMPIRE_FEEDING_RANGE.clamp(vampireFeedingRange);
    vampireChaseRange = VAMPIRE_CHASE_RANGE.clamp(vampireChaseRange);
    vampireHomeRadius = VAMPIRE_HOME_RADIUS.clamp(vampireHomeRadius);
    vampireAlertRange = VAMPIRE_ALERT_RANGE.clamp(vampireAlertRange);
    eldestHealthPerPlayer = ELDEST_HEALTH_PER_PLAYER.clamp(eldestHealthPerPlayer);
    eldestCrowdRange = ELDEST_CROWD_RANGE.clamp(eldestCrowdRange);
  }

  static MobSettings from(final SettingValues values) {
    return new MobSettings(
        values.get(VAMPIRE_BAT_SPAWN_CHANCE),
        values.get(BAT_ESCORT_CHANCE),
        values.get(VAMPIRE_LEVITATE_SECONDS),
        values.get(VAMPIRE_LAND_SECONDS),
        values.get(VAMPIRE_HISS_SECONDS),
        values.get(VAMPIRE_HUNGER_THRESHOLD),
        values.get(VAMPIRE_BLOOD_DRAIN_SECONDS),
        values.get(VAMPIRE_OUTING_CHANCE),
        values.get(VAMPIRE_FEEDING_RANGE),
        values.get(VAMPIRE_CHASE_RANGE),
        values.get(VAMPIRE_HOME_RADIUS),
        values.get(VAMPIRE_ALERT_RANGE),
        values.get(ELDEST_HEALTH_PER_PLAYER),
        values.get(ELDEST_CROWD_RANGE));
  }
}
