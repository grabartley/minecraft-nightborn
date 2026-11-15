package com.grahambartley.nightborn.sickness;

public final class SicknessRules {

  static final int REMINDER_TICKS = 6000;
  static final int DISPLAY_DRIFT_TICKS = 40;

  private SicknessRules() {}

  public static boolean cures(
      final boolean curingFood, final boolean weakened, final boolean sick) {
    return curingFood && weakened && sick;
  }

  public static boolean remindsOfDaysLeft(
      final int daysLeftBefore, final int daysLeftNow, final int age) {
    return daysLeftBefore != daysLeftNow || age % REMINDER_TICKS == 0;
  }

  public static boolean refreshesDisplay(
      final boolean shown,
      final int shownAmplifier,
      final int shownDuration,
      final int dayIndex,
      final int ticksLeft) {
    return !shown
        || shownAmplifier != dayIndex
        || Math.abs(shownDuration - ticksLeft) > DISPLAY_DRIFT_TICKS;
  }
}
