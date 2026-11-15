package com.grahambartley.nightborn.familiar;

public final class HomeRules {

  public static final int NIGHT_RANGE = 3;

  private HomeRules() {}

  public static int range(final boolean workingHours, final int guardRange) {
    return workingHours ? guardRange : NIGHT_RANGE;
  }

  public static boolean drivesOff(
      final boolean creeper, final double distanceToHomeSquared, final int guardRange) {
    return !creeper && distanceToHomeSquared <= (double) guardRange * guardRange;
  }
}
