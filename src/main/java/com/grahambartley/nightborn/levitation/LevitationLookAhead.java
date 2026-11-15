package com.grahambartley.nightborn.levitation;

import java.util.Optional;
import java.util.OptionalDouble;

public final class LevitationLookAhead {

  static final double BODY_MARGIN = 0.35;
  static final double LEAD_TICKS = 10.0;
  static final double MAX_REACH = 3.5;
  static final int SAMPLES = 4;
  static final double STILL = 1.0E-4;

  private LevitationLookAhead() {}

  public record Heading(double x, double z) {}

  public static Optional<Heading> heading(
      final double strafe, final double forward, final float yawDegrees) {
    final double length = Math.sqrt(strafe * strafe + forward * forward);
    if (length < STILL) {
      return Optional.empty();
    }
    final double yaw = Math.toRadians(yawDegrees);
    final double sin = Math.sin(yaw);
    final double cos = Math.cos(yaw);
    return Optional.of(
        new Heading(
            (strafe * cos - forward * sin) / length, (forward * cos + strafe * sin) / length));
  }

  public static double[] distances(final double horizontalSpeed) {
    final double reach = Math.min(MAX_REACH, BODY_MARGIN + horizontalSpeed * LEAD_TICKS);
    final double[] distances = new double[SAMPLES];
    for (int sample = 0; sample < SAMPLES; sample++) {
      distances[sample] = reach * (sample + 1) / SAMPLES;
    }
    return distances;
  }

  public static OptionalDouble higher(final OptionalDouble first, final OptionalDouble second) {
    if (first.isEmpty()) {
      return second;
    }
    if (second.isEmpty()) {
      return first;
    }
    return OptionalDouble.of(Math.max(first.getAsDouble(), second.getAsDouble()));
  }
}
