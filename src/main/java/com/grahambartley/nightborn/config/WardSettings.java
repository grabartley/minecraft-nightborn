package com.grahambartley.nightborn.config;

import java.util.List;

public record WardSettings(
    int stringRange,
    int stringStrength,
    int stringCap,
    int touchStrength,
    int touchSeconds,
    int crucifixRange,
    int crucifixStrength,
    int shapeRange,
    int shapeStrength,
    int faithBonus,
    int eatenDamage,
    int eatenSeconds,
    int eatenBlood) {

  static final String SECTION = "wards";

  public static final IntSetting STRING_RANGE =
      new IntSetting(SECTION, "stringRange", 8, 1, 32, c -> c.wards().stringRange());
  public static final IntSetting STRING_STRENGTH =
      new IntSetting(SECTION, "stringStrength", 15, 0, 100, c -> c.wards().stringStrength());
  public static final IntSetting STRING_CAP =
      new IntSetting(SECTION, "stringCap", 45, 0, 100, c -> c.wards().stringCap());
  public static final IntSetting TOUCH_STRENGTH =
      new IntSetting(SECTION, "touchStrength", 80, 0, 100, c -> c.wards().touchStrength());
  public static final IntSetting TOUCH_SECONDS =
      new IntSetting(SECTION, "touchSeconds", 3, 1, 30, c -> c.wards().touchSeconds());
  public static final IntSetting CRUCIFIX_RANGE =
      new IntSetting(SECTION, "crucifixRange", 6, 1, 32, c -> c.wards().crucifixRange());
  public static final IntSetting CRUCIFIX_STRENGTH =
      new IntSetting(SECTION, "crucifixStrength", 60, 0, 100, c -> c.wards().crucifixStrength());
  public static final IntSetting SHAPE_RANGE =
      new IntSetting(SECTION, "shapeRange", 8, 1, 32, c -> c.wards().shapeRange());
  public static final IntSetting SHAPE_STRENGTH =
      new IntSetting(SECTION, "shapeStrength", 40, 0, 100, c -> c.wards().shapeStrength());
  public static final IntSetting FAITH_BONUS =
      new IntSetting(SECTION, "faithBonus", 25, 0, 100, c -> c.wards().faithBonus());

  public static final IntSetting EATEN_DAMAGE =
      new IntSetting(SECTION, "eatenDamage", 12, 0, 40, c -> c.wards().eatenDamage());
  public static final IntSetting EATEN_SECONDS =
      new IntSetting(SECTION, "eatenSeconds", 60, 1, 600, c -> c.wards().eatenSeconds());
  public static final IntSetting EATEN_BLOOD =
      new IntSetting(SECTION, "eatenBlood", 6, 0, 20, c -> c.wards().eatenBlood());

  public static final List<Setting<?>> SETTINGS =
      List.of(
          STRING_RANGE,
          STRING_STRENGTH,
          STRING_CAP,
          TOUCH_STRENGTH,
          TOUCH_SECONDS,
          CRUCIFIX_RANGE,
          CRUCIFIX_STRENGTH,
          SHAPE_RANGE,
          SHAPE_STRENGTH,
          FAITH_BONUS,
          EATEN_DAMAGE,
          EATEN_SECONDS,
          EATEN_BLOOD);

  public WardSettings {
    stringRange = STRING_RANGE.clamp(stringRange);
    stringStrength = STRING_STRENGTH.clamp(stringStrength);
    stringCap = STRING_CAP.clamp(stringCap);
    touchStrength = TOUCH_STRENGTH.clamp(touchStrength);
    touchSeconds = TOUCH_SECONDS.clamp(touchSeconds);
    crucifixRange = CRUCIFIX_RANGE.clamp(crucifixRange);
    crucifixStrength = CRUCIFIX_STRENGTH.clamp(crucifixStrength);
    shapeRange = SHAPE_RANGE.clamp(shapeRange);
    shapeStrength = SHAPE_STRENGTH.clamp(shapeStrength);
    faithBonus = FAITH_BONUS.clamp(faithBonus);
    eatenDamage = EATEN_DAMAGE.clamp(eatenDamage);
    eatenSeconds = EATEN_SECONDS.clamp(eatenSeconds);
    eatenBlood = EATEN_BLOOD.clamp(eatenBlood);
  }

  static WardSettings from(final SettingValues values) {
    return new WardSettings(
        values.get(STRING_RANGE),
        values.get(STRING_STRENGTH),
        values.get(STRING_CAP),
        values.get(TOUCH_STRENGTH),
        values.get(TOUCH_SECONDS),
        values.get(CRUCIFIX_RANGE),
        values.get(CRUCIFIX_STRENGTH),
        values.get(SHAPE_RANGE),
        values.get(SHAPE_STRENGTH),
        values.get(FAITH_BONUS),
        values.get(EATEN_DAMAGE),
        values.get(EATEN_SECONDS),
        values.get(EATEN_BLOOD));
  }
}
