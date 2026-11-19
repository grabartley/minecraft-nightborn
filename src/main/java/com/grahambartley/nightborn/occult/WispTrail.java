package com.grahambartley.nightborn.occult;

public final class WispTrail {

  static final int EVERY_TICKS = 3;
  static final int FADING_EVERY_TICKS = 1;
  static final double SPREAD = 0.25;
  static final double HEIGHT = 0.1;
  static final double DRIFT_UP = 0.012;

  private WispTrail() {}

  public static boolean sheds(final int age, final boolean fading) {
    return age % (fading ? FADING_EVERY_TICKS : EVERY_TICKS) == 0;
  }

  public static double offset(final double roll) {
    return (roll * 2.0 - 1.0) * SPREAD;
  }

  public static double height() {
    return HEIGHT;
  }

  public static double driftUp() {
    return DRIFT_UP;
  }
}
