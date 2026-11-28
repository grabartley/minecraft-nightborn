package com.grahambartley.nightborn.config;

import java.util.List;

public record GearSettings(
    int silverDamage,
    int holyWaterDamage,
    int holyWaterStrength,
    int holyWaterSeconds,
    int garlicBombStrength,
    int garlicBombSeconds,
    int garlicBombRadius,
    int garlicBombCloudSeconds) {

  static final String SECTION = "gear";

  public static final IntSetting SILVER_DAMAGE =
      new IntSetting(SECTION, "silverDamage", 4, 0, 20, c -> c.gear().silverDamage());
  public static final IntSetting HOLY_WATER_DAMAGE =
      new IntSetting(SECTION, "holyWaterDamage", 6, 0, 40, c -> c.gear().holyWaterDamage());
  public static final IntSetting HOLY_WATER_STRENGTH =
      new IntSetting(SECTION, "holyWaterStrength", 40, 0, 100, c -> c.gear().holyWaterStrength());
  public static final IntSetting HOLY_WATER_SECONDS =
      new IntSetting(SECTION, "holyWaterSeconds", 10, 1, 120, c -> c.gear().holyWaterSeconds());
  public static final IntSetting GARLIC_BOMB_STRENGTH =
      new IntSetting(SECTION, "garlicBombStrength", 60, 0, 100, c -> c.gear().garlicBombStrength());
  public static final IntSetting GARLIC_BOMB_SECONDS =
      new IntSetting(SECTION, "garlicBombSeconds", 5, 1, 60, c -> c.gear().garlicBombSeconds());
  public static final IntSetting GARLIC_BOMB_RADIUS =
      new IntSetting(SECTION, "garlicBombRadius", 3, 1, 8, c -> c.gear().garlicBombRadius());
  public static final IntSetting GARLIC_BOMB_CLOUD_SECONDS =
      new IntSetting(
          SECTION, "garlicBombCloudSeconds", 10, 1, 60, c -> c.gear().garlicBombCloudSeconds());

  public static final List<Setting<?>> SETTINGS =
      List.of(
          SILVER_DAMAGE,
          HOLY_WATER_DAMAGE,
          HOLY_WATER_STRENGTH,
          HOLY_WATER_SECONDS,
          GARLIC_BOMB_STRENGTH,
          GARLIC_BOMB_SECONDS,
          GARLIC_BOMB_RADIUS,
          GARLIC_BOMB_CLOUD_SECONDS);

  public GearSettings {
    silverDamage = SILVER_DAMAGE.clamp(silverDamage);
    holyWaterDamage = HOLY_WATER_DAMAGE.clamp(holyWaterDamage);
    holyWaterStrength = HOLY_WATER_STRENGTH.clamp(holyWaterStrength);
    holyWaterSeconds = HOLY_WATER_SECONDS.clamp(holyWaterSeconds);
    garlicBombStrength = GARLIC_BOMB_STRENGTH.clamp(garlicBombStrength);
    garlicBombSeconds = GARLIC_BOMB_SECONDS.clamp(garlicBombSeconds);
    garlicBombRadius = GARLIC_BOMB_RADIUS.clamp(garlicBombRadius);
    garlicBombCloudSeconds = GARLIC_BOMB_CLOUD_SECONDS.clamp(garlicBombCloudSeconds);
  }

  static GearSettings from(final SettingValues values) {
    return new GearSettings(
        values.get(SILVER_DAMAGE),
        values.get(HOLY_WATER_DAMAGE),
        values.get(HOLY_WATER_STRENGTH),
        values.get(HOLY_WATER_SECONDS),
        values.get(GARLIC_BOMB_STRENGTH),
        values.get(GARLIC_BOMB_SECONDS),
        values.get(GARLIC_BOMB_RADIUS),
        values.get(GARLIC_BOMB_CLOUD_SECONDS));
  }
}
