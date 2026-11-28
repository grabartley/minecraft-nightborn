package com.grahambartley.nightborn.config;

import com.grahambartley.nightborn.rank.RankNeeds;
import java.util.List;

public record SkillSettings(
    int firstPointExperience,
    int pointCostRise,
    int bloodExperience,
    int drainBonusExperience,
    int huntExperience,
    int stageExperience,
    int stakePenalty,
    int contractExperience,
    int hunterRankPoints,
    int veteranRankPoints,
    int masterRankPoints,
    int holdGraceSeconds) {

  static final String SECTION = "skills";

  public static final IntSetting FIRST_POINT_EXPERIENCE =
      new IntSetting(
          SECTION, "firstPointExperience", 200, 1, 100000, c -> c.skills().firstPointExperience());
  public static final IntSetting POINT_COST_RISE =
      new IntSetting(SECTION, "pointCostRise", 1, 0, 100, c -> c.skills().pointCostRise());
  public static final IntSetting BLOOD_EXPERIENCE =
      new IntSetting(SECTION, "bloodExperience", 8, 0, 1000, c -> c.skills().bloodExperience());
  public static final IntSetting DRAIN_BONUS_EXPERIENCE =
      new IntSetting(
          SECTION, "drainBonusExperience", 100, 0, 10000, c -> c.skills().drainBonusExperience());
  public static final IntSetting HUNT_EXPERIENCE =
      new IntSetting(SECTION, "huntExperience", 100, 0, 10000, c -> c.skills().huntExperience());
  public static final IntSetting STAGE_EXPERIENCE =
      new IntSetting(SECTION, "stageExperience", 150, 0, 10000, c -> c.skills().stageExperience());
  public static final IntSetting STAKE_PENALTY =
      new IntSetting(SECTION, "stakePenalty", 10, 0, 100, c -> c.skills().stakePenalty());
  public static final IntSetting CONTRACT_EXPERIENCE =
      new IntSetting(
          SECTION, "contractExperience", 250, 0, 10000, c -> c.skills().contractExperience());

  public static final IntSetting HUNTER_RANK_POINTS =
      new IntSetting(SECTION, "hunterRankPoints", 10, 0, 1000, c -> c.skills().hunterRankPoints());
  public static final IntSetting VETERAN_RANK_POINTS =
      new IntSetting(
          SECTION, "veteranRankPoints", 45, 0, 1000, c -> c.skills().veteranRankPoints());
  public static final IntSetting MASTER_RANK_POINTS =
      new IntSetting(SECTION, "masterRankPoints", 100, 0, 1000, c -> c.skills().masterRankPoints());

  public static final IntSetting HOLD_GRACE_SECONDS =
      new IntSetting(SECTION, "holdGraceSeconds", 3, 0, 60, c -> c.skills().holdGraceSeconds());

  public static final List<Setting<?>> SETTINGS =
      List.of(
          FIRST_POINT_EXPERIENCE,
          POINT_COST_RISE,
          BLOOD_EXPERIENCE,
          DRAIN_BONUS_EXPERIENCE,
          HUNT_EXPERIENCE,
          STAGE_EXPERIENCE,
          STAKE_PENALTY,
          CONTRACT_EXPERIENCE,
          HUNTER_RANK_POINTS,
          VETERAN_RANK_POINTS,
          MASTER_RANK_POINTS,
          HOLD_GRACE_SECONDS);

  public SkillSettings {
    firstPointExperience = FIRST_POINT_EXPERIENCE.clamp(firstPointExperience);
    pointCostRise = POINT_COST_RISE.clamp(pointCostRise);
    bloodExperience = BLOOD_EXPERIENCE.clamp(bloodExperience);
    drainBonusExperience = DRAIN_BONUS_EXPERIENCE.clamp(drainBonusExperience);
    huntExperience = HUNT_EXPERIENCE.clamp(huntExperience);
    stageExperience = STAGE_EXPERIENCE.clamp(stageExperience);
    stakePenalty = STAKE_PENALTY.clamp(stakePenalty);
    contractExperience = CONTRACT_EXPERIENCE.clamp(contractExperience);
    hunterRankPoints = HUNTER_RANK_POINTS.clamp(hunterRankPoints);
    veteranRankPoints = VETERAN_RANK_POINTS.clamp(veteranRankPoints);
    masterRankPoints = MASTER_RANK_POINTS.clamp(masterRankPoints);
    holdGraceSeconds = HOLD_GRACE_SECONDS.clamp(holdGraceSeconds);
  }

  public RankNeeds rankNeeds() {
    return new RankNeeds(List.of(hunterRankPoints, veteranRankPoints, masterRankPoints));
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
        values.get(CONTRACT_EXPERIENCE),
        values.get(HUNTER_RANK_POINTS),
        values.get(VETERAN_RANK_POINTS),
        values.get(MASTER_RANK_POINTS),
        values.get(HOLD_GRACE_SECONDS));
  }
}
