package com.grahambartley.nightborn.sickness;

public record SicknessStage(
    int stage, int daysLeft, boolean hunger, boolean weakness, boolean daylightSlowness) {

  public static final int TICKS_PER_DAY = 24000;
  public static final int STAGES = 3;

  public static int totalTicks(final int days) {
    return Math.max(1, days) * TICKS_PER_DAY;
  }

  public static int ticksLeft(final int ticksRun, final int days) {
    return Math.max(0, totalTicks(days) - Math.max(0, ticksRun));
  }

  public static int advance(final int ticksRun, final int step) {
    return (int) Math.min(Integer.MAX_VALUE, (long) Math.max(0, ticksRun) + Math.max(0, step));
  }

  public static SicknessStage of(final int ticksRun, final int days) {
    final int total = totalTicks(days);
    final int left = ticksLeft(ticksRun, days);
    final int stage = Math.min(STAGES, (int) ((long) (total - left) * STAGES / total) + 1);
    final int daysLeft = (left + TICKS_PER_DAY - 1) / TICKS_PER_DAY;
    return new SicknessStage(stage, daysLeft, stage >= 1, stage >= 2, stage >= 3);
  }
}
