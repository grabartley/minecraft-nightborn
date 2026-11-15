package com.grahambartley.nightborn.config;

import java.util.List;

public record SunSettings(int sunDamage) {

  static final String SECTION = "sun";

  public static final IntSetting SUN_DAMAGE =
      new IntSetting(SECTION, "sunDamage", 2, 1, 40, c -> c.sun().sunDamage());

  public static final List<Setting<?>> SETTINGS = List.of(SUN_DAMAGE);

  public SunSettings {
    sunDamage = SUN_DAMAGE.clamp(sunDamage);
  }

  static SunSettings from(final SettingValues values) {
    return new SunSettings(values.get(SUN_DAMAGE));
  }
}
