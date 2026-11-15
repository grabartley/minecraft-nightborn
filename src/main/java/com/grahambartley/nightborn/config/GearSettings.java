package com.grahambartley.nightborn.config;

import java.util.List;

public record GearSettings(int stakeDamage, int silverDamage) {

  static final String SECTION = "gear";

  public static final IntSetting STAKE_DAMAGE =
      new IntSetting(SECTION, "stakeDamage", 4, 0, 20, c -> c.gear().stakeDamage());
  public static final IntSetting SILVER_DAMAGE =
      new IntSetting(SECTION, "silverDamage", 4, 0, 20, c -> c.gear().silverDamage());

  public static final List<Setting<?>> SETTINGS = List.of(STAKE_DAMAGE, SILVER_DAMAGE);

  public GearSettings {
    stakeDamage = STAKE_DAMAGE.clamp(stakeDamage);
    silverDamage = SILVER_DAMAGE.clamp(silverDamage);
  }

  static GearSettings from(final SettingValues values) {
    return new GearSettings(values.get(STAKE_DAMAGE), values.get(SILVER_DAMAGE));
  }
}
