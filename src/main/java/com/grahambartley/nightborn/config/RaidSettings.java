package com.grahambartley.nightborn.config;

import java.util.List;

public record RaidSettings(int wakeNoise, int noiseFade, int watchDays, int contractMapSpread) {

  static final String SECTION = "raids";

  public static final IntSetting WAKE_NOISE =
      new IntSetting(SECTION, "wakeNoise", 10, 1, 1000, c -> c.raids().wakeNoise());
  public static final IntSetting NOISE_FADE =
      new IntSetting(SECTION, "noiseFade", 1, 0, 100, c -> c.raids().noiseFade());
  public static final IntSetting WATCH_DAYS =
      new IntSetting(SECTION, "watchDays", 3, 0, 100, c -> c.raids().watchDays());
  public static final IntSetting CONTRACT_MAP_SPREAD =
      new IntSetting(SECTION, "contractMapSpread", 64, 0, 512, c -> c.raids().contractMapSpread());

  public static final List<Setting<?>> SETTINGS =
      List.of(WAKE_NOISE, NOISE_FADE, WATCH_DAYS, CONTRACT_MAP_SPREAD);

  public RaidSettings {
    wakeNoise = WAKE_NOISE.clamp(wakeNoise);
    noiseFade = NOISE_FADE.clamp(noiseFade);
    watchDays = WATCH_DAYS.clamp(watchDays);
    contractMapSpread = CONTRACT_MAP_SPREAD.clamp(contractMapSpread);
  }

  static RaidSettings from(final SettingValues values) {
    return new RaidSettings(
        values.get(WAKE_NOISE),
        values.get(NOISE_FADE),
        values.get(WATCH_DAYS),
        values.get(CONTRACT_MAP_SPREAD));
  }
}
