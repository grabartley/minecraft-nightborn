package com.grahambartley.nightborn.config;

import java.util.List;

public record CoatingSettings(
    int holyWaterDamage,
    int holyWaterHits,
    int holyWaterSeconds,
    int holyWaterStrength,
    int holyWaterWeakenSeconds,
    int garlicDamage,
    int garlicHits,
    int garlicSeconds,
    int garlicStrength,
    int garlicWeakenSeconds) {

  static final String SECTION = "coatings";

  public static final IntSetting HOLY_WATER_DAMAGE =
      new IntSetting(SECTION, "holyWaterDamage", 3, 0, 19, c -> c.coatings().holyWaterDamage());
  public static final IntSetting HOLY_WATER_HITS =
      new IntSetting(SECTION, "holyWaterHits", 20, 1, 500, c -> c.coatings().holyWaterHits());
  public static final IntSetting HOLY_WATER_SECONDS =
      new IntSetting(
          SECTION, "holyWaterSeconds", 300, 10, 3600, c -> c.coatings().holyWaterSeconds());
  public static final IntSetting HOLY_WATER_STRENGTH =
      new IntSetting(
          SECTION, "holyWaterStrength", 20, 0, 100, c -> c.coatings().holyWaterStrength());
  public static final IntSetting HOLY_WATER_WEAKEN_SECONDS =
      new IntSetting(
          SECTION, "holyWaterWeakenSeconds", 3, 1, 60, c -> c.coatings().holyWaterWeakenSeconds());
  public static final IntSetting GARLIC_DAMAGE =
      new IntSetting(SECTION, "garlicDamage", 2, 0, 19, c -> c.coatings().garlicDamage());
  public static final IntSetting GARLIC_HITS =
      new IntSetting(SECTION, "garlicHits", 20, 1, 500, c -> c.coatings().garlicHits());
  public static final IntSetting GARLIC_SECONDS =
      new IntSetting(SECTION, "garlicSeconds", 300, 10, 3600, c -> c.coatings().garlicSeconds());
  public static final IntSetting GARLIC_STRENGTH =
      new IntSetting(SECTION, "garlicStrength", 15, 0, 100, c -> c.coatings().garlicStrength());
  public static final IntSetting GARLIC_WEAKEN_SECONDS =
      new IntSetting(
          SECTION, "garlicWeakenSeconds", 3, 1, 60, c -> c.coatings().garlicWeakenSeconds());

  public static final List<Setting<?>> SETTINGS =
      List.of(
          HOLY_WATER_DAMAGE,
          HOLY_WATER_HITS,
          HOLY_WATER_SECONDS,
          HOLY_WATER_STRENGTH,
          HOLY_WATER_WEAKEN_SECONDS,
          GARLIC_DAMAGE,
          GARLIC_HITS,
          GARLIC_SECONDS,
          GARLIC_STRENGTH,
          GARLIC_WEAKEN_SECONDS);

  public CoatingSettings {
    holyWaterDamage = HOLY_WATER_DAMAGE.clamp(holyWaterDamage);
    holyWaterHits = HOLY_WATER_HITS.clamp(holyWaterHits);
    holyWaterSeconds = HOLY_WATER_SECONDS.clamp(holyWaterSeconds);
    holyWaterStrength = HOLY_WATER_STRENGTH.clamp(holyWaterStrength);
    holyWaterWeakenSeconds = HOLY_WATER_WEAKEN_SECONDS.clamp(holyWaterWeakenSeconds);
    garlicDamage = GARLIC_DAMAGE.clamp(garlicDamage);
    garlicHits = GARLIC_HITS.clamp(garlicHits);
    garlicSeconds = GARLIC_SECONDS.clamp(garlicSeconds);
    garlicStrength = GARLIC_STRENGTH.clamp(garlicStrength);
    garlicWeakenSeconds = GARLIC_WEAKEN_SECONDS.clamp(garlicWeakenSeconds);
  }

  static CoatingSettings from(final SettingValues values) {
    return new CoatingSettings(
        values.get(HOLY_WATER_DAMAGE),
        values.get(HOLY_WATER_HITS),
        values.get(HOLY_WATER_SECONDS),
        values.get(HOLY_WATER_STRENGTH),
        values.get(HOLY_WATER_WEAKEN_SECONDS),
        values.get(GARLIC_DAMAGE),
        values.get(GARLIC_HITS),
        values.get(GARLIC_SECONDS),
        values.get(GARLIC_STRENGTH),
        values.get(GARLIC_WEAKEN_SECONDS));
  }
}
