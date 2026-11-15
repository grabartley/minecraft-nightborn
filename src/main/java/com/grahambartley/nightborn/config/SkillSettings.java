package com.grahambartley.nightborn.config;

import java.util.List;

public record SkillSettings(int bloodPerPoint, int drainBonusPoints) {

  static final String SECTION = "skills";

  public static final IntSetting BLOOD_PER_POINT =
      new IntSetting(SECTION, "bloodPerPoint", 20, 1, 1000, c -> c.skills().bloodPerPoint());
  public static final IntSetting DRAIN_BONUS_POINTS =
      new IntSetting(SECTION, "drainBonusPoints", 1, 0, 20, c -> c.skills().drainBonusPoints());

  public static final List<Setting<?>> SETTINGS = List.of(BLOOD_PER_POINT, DRAIN_BONUS_POINTS);

  public SkillSettings {
    bloodPerPoint = BLOOD_PER_POINT.clamp(bloodPerPoint);
    drainBonusPoints = DRAIN_BONUS_POINTS.clamp(drainBonusPoints);
  }

  static SkillSettings from(final SettingValues values) {
    return new SkillSettings(values.get(BLOOD_PER_POINT), values.get(DRAIN_BONUS_POINTS));
  }
}
