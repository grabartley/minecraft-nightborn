package com.grahambartley.nightborn.config;

import java.util.List;

public record ServantSettings(int gatherRange, int scoutRange, int scoutSight, int watchRange) {

  static final String SECTION = "servants";

  public static final IntSetting GATHER_RANGE =
      new IntSetting(SECTION, "gatherRange", 8, 2, 24, c -> c.servants().gatherRange());
  public static final IntSetting SCOUT_RANGE =
      new IntSetting(SECTION, "scoutRange", 256, 32, 1024, c -> c.servants().scoutRange());
  public static final IntSetting SCOUT_SIGHT =
      new IntSetting(SECTION, "scoutSight", 24, 8, 32, c -> c.servants().scoutSight());
  public static final IntSetting WATCH_RANGE =
      new IntSetting(SECTION, "watchRange", 16, 4, 32, c -> c.servants().watchRange());

  public static final List<Setting<?>> SETTINGS =
      List.of(GATHER_RANGE, SCOUT_RANGE, SCOUT_SIGHT, WATCH_RANGE);

  public ServantSettings {
    gatherRange = GATHER_RANGE.clamp(gatherRange);
    scoutRange = SCOUT_RANGE.clamp(scoutRange);
    scoutSight = SCOUT_SIGHT.clamp(scoutSight);
    watchRange = WATCH_RANGE.clamp(watchRange);
  }

  static ServantSettings from(final SettingValues values) {
    return new ServantSettings(
        values.get(GATHER_RANGE),
        values.get(SCOUT_RANGE),
        values.get(SCOUT_SIGHT),
        values.get(WATCH_RANGE));
  }
}
