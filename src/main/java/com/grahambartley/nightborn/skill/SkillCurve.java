package com.grahambartley.nightborn.skill;

public final class SkillCurve {

  public static final int MAX_COST = 1_000_000_000;

  public record Gain(int earned, float experience) {}

  private SkillCurve() {}

  public static int cost(final int pointsEarned, final int firstCost, final int risePercent) {
    final double cost =
        Math.max(1, firstCost)
            * Math.pow(1.0 + Math.max(0, risePercent) / 100.0, Math.max(0, pointsEarned));
    return (int) Math.max(1L, Math.min(MAX_COST, Math.round(cost)));
  }

  public static Gain add(
      final int earned,
      final float experience,
      final float gained,
      final int firstCost,
      final int risePercent,
      final int maxPoints) {
    int points = earned;
    float banked = experience + Math.max(0.0F, gained);
    while (points < maxPoints && banked >= cost(points, firstCost, risePercent)) {
      banked -= cost(points, firstCost, risePercent);
      points++;
    }
    return new Gain(points, points >= maxPoints ? 0.0F : banked);
  }

  public static float fraction(
      final int earned, final float experience, final int firstCost, final int risePercent) {
    return Math.max(0.0F, Math.min(1.0F, experience / cost(earned, firstCost, risePercent)));
  }
}
