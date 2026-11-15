package com.grahambartley.nightborn.levitation;

import java.util.OptionalDouble;

public record LevitationGuard(int strikes, double lowestFeet) {

  public static final LevitationGuard FRESH = new LevitationGuard(0, Double.NaN);

  static final double TOLERANCE = 0.75;
  static final double DESCENT = LevitationLift.SINK_SPEED / 2.0;
  static final int STRIKE_LIMIT = 40;

  public LevitationGuard next(final double feet, final OptionalDouble surface) {
    final boolean tooHigh =
        surface.isEmpty() || feet > surface.getAsDouble() + LevitationLift.HOVER_HEIGHT + TOLERANCE;
    if (!tooHigh) {
      return new LevitationGuard(0, feet);
    }
    if (Double.isNaN(lowestFeet)) {
      return new LevitationGuard(strikes + 1, feet);
    }
    final boolean descending = feet < lowestFeet - DESCENT;
    return new LevitationGuard(descending ? strikes : strikes + 1, Math.min(lowestFeet, feet));
  }

  public boolean lands() {
    return strikes >= STRIKE_LIMIT;
  }
}
