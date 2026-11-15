package com.grahambartley.nightborn.config;

import java.util.List;

public record CoffinSettings(int sleepShare) {

  static final String SECTION = "coffins";

  public static final IntSetting SLEEP_SHARE =
      new IntSetting(SECTION, "sleepShare", 100, 0, 100, c -> c.coffins().sleepShare());

  public static final List<Setting<?>> SETTINGS = List.of(SLEEP_SHARE);

  public CoffinSettings {
    sleepShare = SLEEP_SHARE.clamp(sleepShare);
  }

  static CoffinSettings from(final SettingValues values) {
    return new CoffinSettings(values.get(SLEEP_SHARE));
  }
}
