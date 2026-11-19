package com.grahambartley.nightborn.config;

import java.util.List;

public record CoffinSettings(
    int sleepShare,
    int wholeDayWindow,
    int restedSpeed,
    int restedDamage,
    int houseRestedSpeed,
    int houseRestedDamage) {

  static final String SECTION = "coffins";

  public static final IntSetting SLEEP_SHARE =
      new IntSetting(SECTION, "sleepShare", 50, 0, 100, c -> c.coffins().sleepShare());
  public static final IntSetting WHOLE_DAY_WINDOW =
      new IntSetting(SECTION, "wholeDayWindow", 50, 0, 600, c -> c.coffins().wholeDayWindow());
  public static final IntSetting RESTED_SPEED =
      new IntSetting(SECTION, "restedSpeed", 10, 0, 100, c -> c.coffins().restedSpeed());
  public static final IntSetting RESTED_DAMAGE =
      new IntSetting(SECTION, "restedDamage", 1, 0, 20, c -> c.coffins().restedDamage());
  public static final IntSetting HOUSE_RESTED_SPEED =
      new IntSetting(SECTION, "houseRestedSpeed", 15, 0, 100, c -> c.coffins().houseRestedSpeed());
  public static final IntSetting HOUSE_RESTED_DAMAGE =
      new IntSetting(SECTION, "houseRestedDamage", 2, 0, 20, c -> c.coffins().houseRestedDamage());

  public static final List<Setting<?>> SETTINGS =
      List.of(
          SLEEP_SHARE,
          WHOLE_DAY_WINDOW,
          RESTED_SPEED,
          RESTED_DAMAGE,
          HOUSE_RESTED_SPEED,
          HOUSE_RESTED_DAMAGE);

  public CoffinSettings {
    sleepShare = SLEEP_SHARE.clamp(sleepShare);
    wholeDayWindow = WHOLE_DAY_WINDOW.clamp(wholeDayWindow);
    restedSpeed = RESTED_SPEED.clamp(restedSpeed);
    restedDamage = RESTED_DAMAGE.clamp(restedDamage);
    houseRestedSpeed = HOUSE_RESTED_SPEED.clamp(houseRestedSpeed);
    houseRestedDamage = HOUSE_RESTED_DAMAGE.clamp(houseRestedDamage);
  }

  static CoffinSettings from(final SettingValues values) {
    return new CoffinSettings(
        values.get(SLEEP_SHARE),
        values.get(WHOLE_DAY_WINDOW),
        values.get(RESTED_SPEED),
        values.get(RESTED_DAMAGE),
        values.get(HOUSE_RESTED_SPEED),
        values.get(HOUSE_RESTED_DAMAGE));
  }
}
