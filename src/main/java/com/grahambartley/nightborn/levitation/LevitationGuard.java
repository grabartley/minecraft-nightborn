package com.grahambartley.nightborn.levitation;

import java.util.OptionalDouble;

public record LevitationGuard(int strikes, double lowestFeet) {

  public static final LevitationGuard FRESH = new LevitationGuard(0, Double.NaN);

  static final double TOLERANCE = 0.75;
  static final double DESCENT = 0.01;
  static final int STRIKE_LIMIT = 40;

  public LevitationGuard next(final double feet, final OptionalDouble surface) {
    final boolean descending = !Double.isNaN(lowestFeet) && feet < lowestFeet - DESCENT;
    final boolean tooHigh =
        surface.isEmpty() || feet > surface.getAsDouble() + LevitationLift.HOVER_HEIGHT + TOLERANCE;
    if (tooHigh && !descending) {
      return new LevitationGuard(
          strikes + 1, Double.isNaN(lowestFeet) ? feet : Math.min(lowestFeet, feet));
    }
    return new LevitationGuard(0, feet);
  }

  public boolean lands() {
    return strikes >= STRIKE_LIMIT;
  }
}
