package com.grahambartley.nightborn.config;

import java.util.List;

public record FirstNightSettings(
    int firstNights,
    int watcherChance,
    int watcherDistance,
    int watcherLeaveDistance,
    int signChance,
    int signRange) {

  static final String SECTION = "firstNights";

  public static final IntSetting FIRST_NIGHTS =
      new IntSetting(SECTION, "firstNights", 5, 0, 30, c -> c.firstNights().firstNights());
  public static final IntSetting WATCHER_CHANCE =
      new IntSetting(SECTION, "watcherChance", 50, 0, 100, c -> c.firstNights().watcherChance());
  public static final IntSetting WATCHER_DISTANCE =
      new IntSetting(SECTION, "watcherDistance", 24, 8, 64, c -> c.firstNights().watcherDistance());
  public static final IntSetting WATCHER_LEAVE_DISTANCE =
      new IntSetting(
          SECTION, "watcherLeaveDistance", 12, 2, 32, c -> c.firstNights().watcherLeaveDistance());
  public static final IntSetting SIGN_CHANCE =
      new IntSetting(SECTION, "signChance", 5, 0, 100, c -> c.firstNights().signChance());
  public static final IntSetting SIGN_RANGE =
      new IntSetting(SECTION, "signRange", 128, 32, 512, c -> c.firstNights().signRange());

  public static final List<Setting<?>> SETTINGS =
      List.of(
          FIRST_NIGHTS,
          WATCHER_CHANCE,
          WATCHER_DISTANCE,
          WATCHER_LEAVE_DISTANCE,
          SIGN_CHANCE,
          SIGN_RANGE);

  public FirstNightSettings {
    firstNights = FIRST_NIGHTS.clamp(firstNights);
    watcherChance = WATCHER_CHANCE.clamp(watcherChance);
    watcherDistance = WATCHER_DISTANCE.clamp(watcherDistance);
    watcherLeaveDistance =
        Math.min(watcherDistance - 1, WATCHER_LEAVE_DISTANCE.clamp(watcherLeaveDistance));
    signChance = SIGN_CHANCE.clamp(signChance);
    signRange = SIGN_RANGE.clamp(signRange);
  }

  static FirstNightSettings from(final SettingValues values) {
    return new FirstNightSettings(
        values.get(FIRST_NIGHTS),
        values.get(WATCHER_CHANCE),
        values.get(WATCHER_DISTANCE),
        values.get(WATCHER_LEAVE_DISTANCE),
        values.get(SIGN_CHANCE),
        values.get(SIGN_RANGE));
  }
}
