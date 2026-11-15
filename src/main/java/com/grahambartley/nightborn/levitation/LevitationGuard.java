package com.grahambartley.nightborn.levitation;

import java.util.OptionalDouble;

public record LevitationGuard(int strikes, double lastFeet) {

  public static final LevitationGuard FRESH = new LevitationGuard(0, Double.NaN);

  static final double TOLERANCE = 0.75;
  static final double DESCENT = 0.01;
  static final int STRIKE_LIMIT = 40;

  public LevitationGuard next(final double feet, final OptionalDouble surface) {
    final boolean descending = !Double.isNaN(lastFeet) && feet < lastFeet - DESCENT;
    final boolean tooHigh =
        surface.isEmpty() || feet > surface.getAsDouble() + LevitationLift.HOVER_HEIGHT + TOLERANCE;
    return new LevitationGuard(tooHigh && !descending ? strikes + 1 : 0, feet);
  }

  public boolean lands() {
    return strikes >= STRIKE_LIMIT;
  }
}
