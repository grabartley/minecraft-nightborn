package com.grahambartley.nightborn.config;

import java.util.List;

public record SunSettings(int sunDamage, int parasolSeconds) {

  static final String SECTION = "sun";

  public static final IntSetting SUN_DAMAGE =
      new IntSetting(SECTION, "sunDamage", 2, 1, 40, c -> c.sun().sunDamage());
  public static final IntSetting PARASOL_SECONDS =
      new IntSetting(SECTION, "parasolSeconds", 180, 10, 3600, c -> c.sun().parasolSeconds());

  public static final List<Setting<?>> SETTINGS = List.of(SUN_DAMAGE, PARASOL_SECONDS);

  public SunSettings {
    sunDamage = SUN_DAMAGE.clamp(sunDamage);
    parasolSeconds = PARASOL_SECONDS.clamp(parasolSeconds);
  }

  static SunSettings from(final SettingValues values) {
    return new SunSettings(values.get(SUN_DAMAGE), values.get(PARASOL_SECONDS));
  }
}
