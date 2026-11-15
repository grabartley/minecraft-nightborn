package com.grahambartley.nightborn.sickness;

public final class SicknessRules {

  static final int REMINDER_TICKS = 6000;
  static final int DISPLAY_DRIFT_TICKS = 40;
  public static final int SYMPTOM_TICKS = 60;
  public static final int REFRESH_TICKS = 20;

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
      final int stageIndex,
      final int ticksLeft) {
    return !shown
        || shownAmplifier != stageIndex
        || Math.abs(shownDuration - ticksLeft) > DISPLAY_DRIFT_TICKS;
  }

  public static boolean refreshesSymptom(final boolean present, final int duration) {
    return !present || duration < SYMPTOM_TICKS - REFRESH_TICKS;
  }
}
