package com.grahambartley.nightborn.config;

import java.util.List;

public record WeakenedSettings(int slowness, int weakerBlows) {

  static final String SECTION = "weakened";

  public static final IntSetting SLOWNESS =
      new IntSetting(SECTION, "slowness", 40, 0, 90, c -> c.weakened().slowness());
  public static final IntSetting WEAKER_BLOWS =
      new IntSetting(SECTION, "weakerBlows", 50, 0, 90, c -> c.weakened().weakerBlows());

  public static final List<Setting<?>> SETTINGS = List.of(SLOWNESS, WEAKER_BLOWS);

  public WeakenedSettings {
    slowness = SLOWNESS.clamp(slowness);
    weakerBlows = WEAKER_BLOWS.clamp(weakerBlows);
  }

  static WeakenedSettings from(final SettingValues values) {
    return new WeakenedSettings(values.get(SLOWNESS), values.get(WEAKER_BLOWS));
  }
}
