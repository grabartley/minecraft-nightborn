package com.grahambartley.nightborn.config;

import java.util.List;

public record DismembermentSettings(
    boolean enabled,
    boolean playersMaimed,
    int severChance,
    int strongBlow,
    int massiveBlow,
    int badlyHurt) {

  static final String SECTION = "dismemberment";

  public static final BoolSetting ENABLED =
      new BoolSetting(SECTION, "enabled", true, c -> c.dismemberment().enabled());
  public static final BoolSetting PLAYERS_MAIMED =
      new BoolSetting(SECTION, "playersMaimed", true, c -> c.dismemberment().playersMaimed());
  public static final IntSetting SEVER_CHANCE =
      new IntSetting(SECTION, "severChance", 15, 0, 100, c -> c.dismemberment().severChance());
  public static final IntSetting STRONG_BLOW =
      new IntSetting(SECTION, "strongBlow", 15, 1, 100, c -> c.dismemberment().strongBlow());
  public static final IntSetting MASSIVE_BLOW =
      new IntSetting(SECTION, "massiveBlow", 50, 1, 100, c -> c.dismemberment().massiveBlow());
  public static final IntSetting BADLY_HURT =
      new IntSetting(SECTION, "badlyHurt", 35, 0, 100, c -> c.dismemberment().badlyHurt());

  public static final List<Setting<?>> SETTINGS =
      List.of(ENABLED, PLAYERS_MAIMED, SEVER_CHANCE, STRONG_BLOW, MASSIVE_BLOW, BADLY_HURT);

  public DismembermentSettings {
    severChance = SEVER_CHANCE.clamp(severChance);
    strongBlow = STRONG_BLOW.clamp(strongBlow);
    massiveBlow = MASSIVE_BLOW.clamp(massiveBlow);
    badlyHurt = BADLY_HURT.clamp(badlyHurt);
  }

  static DismembermentSettings from(final SettingValues values) {
    return new DismembermentSettings(
        values.get(ENABLED),
        values.get(PLAYERS_MAIMED),
        values.get(SEVER_CHANCE),
        values.get(STRONG_BLOW),
        values.get(MASSIVE_BLOW),
        values.get(BADLY_HURT));
  }
}
