package com.grahambartley.nightborn.config;

import java.util.List;

public record FeedingSettings(
    int drinkPerSecond,
    int resistChance,
    int sipWeaknessSeconds,
    int witnessRange,
    int bottleSize) {

  static final String SECTION = "feeding";

  public static final IntSetting DRINK_PER_SECOND =
      new IntSetting(SECTION, "drinkPerSecond", 2, 1, 20, c -> c.feeding().drinkPerSecond());
  public static final IntSetting RESIST_CHANCE =
      new IntSetting(SECTION, "resistChance", 10, 0, 100, c -> c.feeding().resistChance());
  public static final IntSetting SIP_WEAKNESS_SECONDS =
      new IntSetting(
          SECTION, "sipWeaknessSeconds", 30, 0, 600, c -> c.feeding().sipWeaknessSeconds());
  public static final IntSetting WITNESS_RANGE =
      new IntSetting(SECTION, "witnessRange", 16, 0, 64, c -> c.feeding().witnessRange());
  public static final IntSetting BOTTLE_SIZE =
      new IntSetting(SECTION, "bottleSize", 4, 1, 20, c -> c.feeding().bottleSize());

  public static final List<Setting<?>> SETTINGS =
      List.of(DRINK_PER_SECOND, RESIST_CHANCE, SIP_WEAKNESS_SECONDS, WITNESS_RANGE, BOTTLE_SIZE);

  public FeedingSettings {
    drinkPerSecond = DRINK_PER_SECOND.clamp(drinkPerSecond);
    resistChance = RESIST_CHANCE.clamp(resistChance);
    sipWeaknessSeconds = SIP_WEAKNESS_SECONDS.clamp(sipWeaknessSeconds);
    witnessRange = WITNESS_RANGE.clamp(witnessRange);
    bottleSize = BOTTLE_SIZE.clamp(bottleSize);
  }

  static FeedingSettings from(final SettingValues values) {
    return new FeedingSettings(
        values.get(DRINK_PER_SECOND),
        values.get(RESIST_CHANCE),
        values.get(SIP_WEAKNESS_SECONDS),
        values.get(WITNESS_RANGE),
        values.get(BOTTLE_SIZE));
  }
}
