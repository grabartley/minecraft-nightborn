package com.grahambartley.nightborn.config;

import com.grahambartley.nightborn.core.NightTime;
import java.util.List;

public record HuntSettings(
    int raidNotoriety,
    int huntNotoriety,
    int notorietyFade,
    int quietNights,
    int warningNights,
    int latestArrival,
    int bellBuildUpTicks) {

  static final String SECTION = "inquisitor";

  public static final IntSetting RAID_NOTORIETY =
      new IntSetting(SECTION, "raidNotoriety", 15, 1, 1000, c -> c.hunt().raidNotoriety());
  public static final IntSetting HUNT_NOTORIETY =
      new IntSetting(SECTION, "huntNotoriety", 30, 1, 1000, c -> c.hunt().huntNotoriety());
  public static final IntSetting NOTORIETY_FADE =
      new IntSetting(SECTION, "notorietyFade", 1, 0, 100, c -> c.hunt().notorietyFade());
  public static final IntSetting QUIET_NIGHTS =
      new IntSetting(SECTION, "quietNights", 10, 0, 100, c -> c.hunt().quietNights());
  public static final IntSetting WARNING_NIGHTS =
      new IntSetting(SECTION, "warningNights", 3, 0, 10, c -> c.hunt().warningNights());
  public static final IntSetting LATEST_ARRIVAL =
      new IntSetting(
          SECTION,
          "latestArrival",
          18000,
          (int) NightTime.DUSK + 1,
          (int) NightTime.DAWN,
          c -> c.hunt().latestArrival());
  public static final IntSetting BELL_BUILD_UP_TICKS =
      new IntSetting(SECTION, "bellBuildUpTicks", 200, 20, 1200, c -> c.hunt().bellBuildUpTicks());

  public static final List<Setting<?>> SETTINGS =
      List.of(
          RAID_NOTORIETY,
          HUNT_NOTORIETY,
          NOTORIETY_FADE,
          QUIET_NIGHTS,
          WARNING_NIGHTS,
          LATEST_ARRIVAL,
          BELL_BUILD_UP_TICKS);

  public HuntSettings {
    raidNotoriety = RAID_NOTORIETY.clamp(raidNotoriety);
    huntNotoriety = Math.max(raidNotoriety, HUNT_NOTORIETY.clamp(huntNotoriety));
    notorietyFade = NOTORIETY_FADE.clamp(notorietyFade);
    quietNights = QUIET_NIGHTS.clamp(quietNights);
    warningNights = WARNING_NIGHTS.clamp(warningNights);
    latestArrival = LATEST_ARRIVAL.clamp(latestArrival);
    bellBuildUpTicks = BELL_BUILD_UP_TICKS.clamp(bellBuildUpTicks);
  }

  static HuntSettings from(final SettingValues values) {
    return new HuntSettings(
        values.get(RAID_NOTORIETY),
        values.get(HUNT_NOTORIETY),
        values.get(NOTORIETY_FADE),
        values.get(QUIET_NIGHTS),
        values.get(WARNING_NIGHTS),
        values.get(LATEST_ARRIVAL),
        values.get(BELL_BUILD_UP_TICKS));
  }
}
