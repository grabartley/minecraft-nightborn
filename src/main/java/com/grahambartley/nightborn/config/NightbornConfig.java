package com.grahambartley.nightborn.config;

import java.util.List;
import java.util.stream.Stream;

public record NightbornConfig(
    TurningSettings turning,
    FeedingSettings feeding,
    BloodSettings blood,
    SunSettings sun,
    DownedSettings downed,
    CoreSettings core,
    WeakenedSettings weakened,
    FactionSettings factions) {

  public static final List<Setting<?>> SETTINGS =
      Stream.of(
              TurningSettings.SETTINGS,
              FeedingSettings.SETTINGS,
              BloodSettings.SETTINGS,
              SunSettings.SETTINGS,
              DownedSettings.SETTINGS,
              CoreSettings.SETTINGS,
              WeakenedSettings.SETTINGS,
              FactionSettings.SETTINGS)
          .flatMap(List::stream)
          .toList();

  public static final NightbornConfig DEFAULTS = from(SettingValues.EMPTY);

  public static NightbornConfig from(final SettingValues values) {
    return new NightbornConfig(
        TurningSettings.from(values),
        FeedingSettings.from(values),
        BloodSettings.from(values),
        SunSettings.from(values),
        DownedSettings.from(values),
        CoreSettings.from(values),
        WeakenedSettings.from(values),
        FactionSettings.from(values));
  }

  public SettingValues values() {
    return SettingValues.of(this);
  }

  public <T> NightbornConfig with(final Setting<T> setting, final T value) {
    return from(values().with(setting, value));
  }

  public static List<String> sections() {
    return SETTINGS.stream().map(Setting::section).distinct().toList();
  }
}
