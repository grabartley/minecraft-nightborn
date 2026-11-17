package com.grahambartley.nightborn.config;

import java.util.List;

public record SkillSettings(
    int firstPointExperience,
    int pointCostRise,
    int bloodExperience,
    int drainBonusExperience,
    int huntExperience,
    int stageExperience,
    int stakePenalty,
    int contractExperience) {

  static final String SECTION = "skills";

  public static final IntSetting FIRST_POINT_EXPERIENCE =
      new IntSetting(
          SECTION, "firstPointExperience", 100, 1, 100000, c -> c.skills().firstPointExperience());
  public static final IntSetting POINT_COST_RISE =
      new IntSetting(SECTION, "pointCostRise", 3, 0, 100, c -> c.skills().pointCostRise());
  public static final IntSetting BLOOD_EXPERIENCE =
      new IntSetting(SECTION, "bloodExperience", 5, 0, 1000, c -> c.skills().bloodExperience());
  public static final IntSetting DRAIN_BONUS_EXPERIENCE =
      new IntSetting(
          SECTION, "drainBonusExperience", 100, 0, 10000, c -> c.skills().drainBonusExperience());
  public static final IntSetting HUNT_EXPERIENCE =
      new IntSetting(SECTION, "huntExperience", 300, 0, 10000, c -> c.skills().huntExperience());
  public static final IntSetting STAGE_EXPERIENCE =
      new IntSetting(SECTION, "stageExperience", 200, 0, 10000, c -> c.skills().stageExperience());
  public static final IntSetting STAKE_PENALTY =
      new IntSetting(SECTION, "stakePenalty", 10, 0, 100, c -> c.skills().stakePenalty());
  public static final IntSetting CONTRACT_EXPERIENCE =
      new IntSetting(
          SECTION, "contractExperience", 500, 0, 10000, c -> c.skills().contractExperience());

  public static final List<Setting<?>> SETTINGS =
      List.of(
          FIRST_POINT_EXPERIENCE,
          POINT_COST_RISE,
          BLOOD_EXPERIENCE,
          DRAIN_BONUS_EXPERIENCE,
          HUNT_EXPERIENCE,
          STAGE_EXPERIENCE,
          STAKE_PENALTY,
          CONTRACT_EXPERIENCE);

  public SkillSettings {
    firstPointExperience = FIRST_POINT_EXPERIENCE.clamp(firstPointExperience);
    pointCostRise = POINT_COST_RISE.clamp(pointCostRise);
    bloodExperience = BLOOD_EXPERIENCE.clamp(bloodExperience);
    drainBonusExperience = DRAIN_BONUS_EXPERIENCE.clamp(drainBonusExperience);
    huntExperience = HUNT_EXPERIENCE.clamp(huntExperience);
    stageExperience = STAGE_EXPERIENCE.clamp(stageExperience);
    stakePenalty = STAKE_PENALTY.clamp(stakePenalty);
    contractExperience = CONTRACT_EXPERIENCE.clamp(contractExperience);
  }

  static SkillSettings from(final SettingValues values) {
    return new SkillSettings(
        values.get(FIRST_POINT_EXPERIENCE),
        values.get(POINT_COST_RISE),
        values.get(BLOOD_EXPERIENCE),
        values.get(DRAIN_BONUS_EXPERIENCE),
        values.get(HUNT_EXPERIENCE),
        values.get(STAGE_EXPERIENCE),
        values.get(STAKE_PENALTY),
        values.get(CONTRACT_EXPERIENCE));
  }
}
