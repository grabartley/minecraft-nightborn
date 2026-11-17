package com.grahambartley.nightborn.config;

import java.util.List;

public record RitualSettings(
    int buildUpTicks, int eldestPoints, int eldestHealthPerPlayer, int fightRange) {

  static final String SECTION = "ritual";

  public static final IntSetting BUILD_UP_TICKS =
      new IntSetting(SECTION, "buildUpTicks", 200, 20, 1200, c -> c.ritual().buildUpTicks());
  public static final IntSetting ELDEST_POINTS =
      new IntSetting(SECTION, "eldestPoints", 40, 0, 500, c -> c.ritual().eldestPoints());
  public static final IntSetting ELDEST_HEALTH_PER_PLAYER =
      new IntSetting(
          SECTION, "eldestHealthPerPlayer", 50, 0, 500, c -> c.ritual().eldestHealthPerPlayer());
  public static final IntSetting FIGHT_RANGE =
      new IntSetting(SECTION, "fightRange", 32, 8, 128, c -> c.ritual().fightRange());

  public static final List<Setting<?>> SETTINGS =
      List.of(BUILD_UP_TICKS, ELDEST_POINTS, ELDEST_HEALTH_PER_PLAYER, FIGHT_RANGE);

  public RitualSettings {
    buildUpTicks = BUILD_UP_TICKS.clamp(buildUpTicks);
    eldestPoints = ELDEST_POINTS.clamp(eldestPoints);
    eldestHealthPerPlayer = ELDEST_HEALTH_PER_PLAYER.clamp(eldestHealthPerPlayer);
    fightRange = FIGHT_RANGE.clamp(fightRange);
  }

  static RitualSettings from(final SettingValues values) {
    return new RitualSettings(
        values.get(BUILD_UP_TICKS),
        values.get(ELDEST_POINTS),
        values.get(ELDEST_HEALTH_PER_PLAYER),
        values.get(FIGHT_RANGE));
  }
}
