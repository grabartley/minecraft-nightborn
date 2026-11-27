package com.grahambartley.nightborn.wild;

public final class WildPace {

  static final int NIGHT_LEG = 48;
  static final int DAY_LEG = 10;
  static final int NIGHT_PAUSE = 40;
  static final int DAY_PAUSE = 100;
  static final int PAUSE_SPREAD = 2;

  private WildPace() {}

  public static boolean travels(
      final boolean roamer, final boolean hasTarget, final boolean sleeping) {
    return roamer && !hasTarget && !sleeping;
  }

  public static int leg(final boolean night) {
    return night ? NIGHT_LEG : DAY_LEG;
  }

  public static int pause(final boolean night, final int roll) {
    final int least = night ? NIGHT_PAUSE : DAY_PAUSE;
    return least + Math.floorMod(roll, least * PAUSE_SPREAD + 1);
  }

  public static boolean mayStop(final boolean night, final boolean skyVisible) {
    return night || !skyVisible;
  }
}
