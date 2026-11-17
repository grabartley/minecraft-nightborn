package com.grahambartley.nightborn.config;

import java.util.List;

public record SkillSettings(
    int bloodPerPoint,
    int drainBonusPoints,
    int huntPoints,
    int stagePoints,
    int stakePenalty,
    int contractPoints,
    int eldestPoints) {

  static final String SECTION = "skills";

  public static final IntSetting BLOOD_PER_POINT =
      new IntSetting(SECTION, "bloodPerPoint", 20, 1, 1000, c -> c.skills().bloodPerPoint());
  public static final IntSetting DRAIN_BONUS_POINTS =
      new IntSetting(SECTION, "drainBonusPoints", 1, 0, 20, c -> c.skills().drainBonusPoints());
  public static final IntSetting HUNT_POINTS =
      new IntSetting(SECTION, "huntPoints", 3, 0, 50, c -> c.skills().huntPoints());
  public static final IntSetting STAGE_POINTS =
      new IntSetting(SECTION, "stagePoints", 2, 0, 50, c -> c.skills().stagePoints());
  public static final IntSetting STAKE_PENALTY =
      new IntSetting(SECTION, "stakePenalty", 10, 0, 100, c -> c.skills().stakePenalty());
  public static final IntSetting CONTRACT_POINTS =
      new IntSetting(SECTION, "contractPoints", 5, 0, 50, c -> c.skills().contractPoints());
  public static final IntSetting ELDEST_POINTS =
      new IntSetting(SECTION, "eldestPoints", 60, 0, 500, c -> c.skills().eldestPoints());

  public static final List<Setting<?>> SETTINGS =
      List.of(
          BLOOD_PER_POINT,
          DRAIN_BONUS_POINTS,
          HUNT_POINTS,
          STAGE_POINTS,
          STAKE_PENALTY,
          CONTRACT_POINTS,
          ELDEST_POINTS);

  public SkillSettings {
    bloodPerPoint = BLOOD_PER_POINT.clamp(bloodPerPoint);
    drainBonusPoints = DRAIN_BONUS_POINTS.clamp(drainBonusPoints);
    huntPoints = HUNT_POINTS.clamp(huntPoints);
    stagePoints = STAGE_POINTS.clamp(stagePoints);
    stakePenalty = STAKE_PENALTY.clamp(stakePenalty);
    contractPoints = CONTRACT_POINTS.clamp(contractPoints);
    eldestPoints = ELDEST_POINTS.clamp(eldestPoints);
  }

  static SkillSettings from(final SettingValues values) {
    return new SkillSettings(
        values.get(BLOOD_PER_POINT),
        values.get(DRAIN_BONUS_POINTS),
        values.get(HUNT_POINTS),
        values.get(STAGE_POINTS),
        values.get(STAKE_PENALTY),
        values.get(CONTRACT_POINTS),
        values.get(ELDEST_POINTS));
  }
}
