package com.grahambartley.nightborn.config;

import java.util.List;
import java.util.stream.Stream;

public record NightbornConfig(
    TurningSettings turning,
    FeedingSettings feeding,
    BloodSettings blood,
    SunSettings sun,
    CoffinSettings coffins,
    DownedSettings downed,
    CoreSettings core,
    WeakenedSettings weakened,
    WardSettings wards,
    FactionSettings factions,
    SkillSettings skills,
    GearSettings gear,
    CoatingSettings coatings,
    ClothingSettings clothing,
    MobSettings mobs,
    StructureSettings structures) {

  public static final List<Setting<?>> SETTINGS =
      Stream.of(
              TurningSettings.SETTINGS,
              FeedingSettings.SETTINGS,
              BloodSettings.SETTINGS,
              SunSettings.SETTINGS,
              CoffinSettings.SETTINGS,
              DownedSettings.SETTINGS,
              CoreSettings.SETTINGS,
              WeakenedSettings.SETTINGS,
              WardSettings.SETTINGS,
              FactionSettings.SETTINGS,
              SkillSettings.SETTINGS,
              GearSettings.SETTINGS,
              CoatingSettings.SETTINGS,
              ClothingSettings.SETTINGS,
              MobSettings.SETTINGS,
              StructureSettings.SETTINGS)
          .flatMap(List::stream)
          .toList();

  public static final NightbornConfig DEFAULTS = from(SettingValues.EMPTY);

  public static NightbornConfig from(final SettingValues values) {
    return new NightbornConfig(
        TurningSettings.from(values),
        FeedingSettings.from(values),
        BloodSettings.from(values),
        SunSettings.from(values),
        CoffinSettings.from(values),
        DownedSettings.from(values),
        CoreSettings.from(values),
        WeakenedSettings.from(values),
        WardSettings.from(values),
        FactionSettings.from(values),
        SkillSettings.from(values),
        GearSettings.from(values),
        CoatingSettings.from(values),
        ClothingSettings.from(values),
        MobSettings.from(values),
        StructureSettings.from(values));
  }

  public SettingValues values() {
    return SettingValues.of(this);
  }

  public <T> NightbornConfig with(final Setting<T> setting, final T value) {
    return from(values().with(setting, value));
  }

  public NightbornConfig withChanges(final NightbornConfig opened, final NightbornConfig edited) {
    SettingValues merged = values();
    final SettingValues before = opened.values();
    final SettingValues after = edited.values();
    for (final Setting<?> setting : SETTINGS) {
      merged = carry(merged, setting, before, after);
    }
    return from(merged);
  }

  public List<Setting<?>> changedFrom(final NightbornConfig other) {
    return SETTINGS.stream().filter(s -> !s.get(this).equals(s.get(other))).toList();
  }

  private static <T> SettingValues carry(
      final SettingValues merged,
      final Setting<T> setting,
      final SettingValues before,
      final SettingValues after) {
    final T edited = after.get(setting);
    return edited.equals(before.get(setting)) ? merged : merged.with(setting, edited);
  }

  public static List<String> sections() {
    return SETTINGS.stream().map(Setting::section).distinct().toList();
  }
}
