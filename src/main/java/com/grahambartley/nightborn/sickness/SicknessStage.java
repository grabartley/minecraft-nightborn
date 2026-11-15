package com.grahambartley.nightborn.sickness;

public record SicknessStage(
    int day, int daysLeft, boolean hunger, boolean weakness, boolean daylightSlowness) {

  public static final int TICKS_PER_DAY = 24000;

  public static int totalTicks(final int days) {
    return Math.max(1, days) * TICKS_PER_DAY;
  }

  public static SicknessStage of(final int ticksLeft, final int days) {
    final int total = totalTicks(days);
    final int left = Math.max(0, Math.min(total, ticksLeft));
    final int day = Math.min(days, (total - left) / TICKS_PER_DAY + 1);
    final int daysLeft = (left + TICKS_PER_DAY - 1) / TICKS_PER_DAY;
    return new SicknessStage(day, daysLeft, day >= 1, day >= 2, day >= 3);
  }
}
