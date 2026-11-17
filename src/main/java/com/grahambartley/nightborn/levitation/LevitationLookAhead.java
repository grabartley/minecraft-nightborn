package com.grahambartley.nightborn.levitation;

import java.util.List;
import java.util.OptionalDouble;

public final class LevitationLookAhead {

  static final double BODY_MARGIN = 0.35;
  static final double LEAD_TICKS = 10.0;
  static final double MAX_REACH = 3.5;
  static final int SAMPLES = 4;
  static final double MAX_CLIMB = 1.0;
  static final double CLIMB_MARGIN = 0.05;
  static final double AHEAD_DEPTH = MAX_CLIMB + LevitationLift.HOVER_HEIGHT + CLIMB_MARGIN;

  private LevitationLookAhead() {}

  public record Sample(boolean blocked, OptionalDouble surface) {}

  public static double[] distances(final double horizontalSpeed) {
    final double reach = Math.min(MAX_REACH, BODY_MARGIN + horizontalSpeed * LEAD_TICKS);
    final double[] distances = new double[SAMPLES];
    for (int sample = 0; sample < SAMPLES; sample++) {
      distances[sample] = reach * (sample + 1) / SAMPLES;
    }
    return distances;
  }

  public static double climbLimit(final OptionalDouble groundBelow, final double feetY) {
    final double ground =
        groundBelow.isPresent() ? groundBelow.getAsDouble() : feetY - LevitationLift.HOVER_HEIGHT;
    return ground + MAX_CLIMB + CLIMB_MARGIN;
  }

  public static OptionalDouble groundToFollow(
      final OptionalDouble below, final List<Sample> nearestFirst) {
    OptionalDouble highest = below;
    for (final Sample sample : nearestFirst) {
      if (sample.blocked()) {
        return highest;
      }
      highest = higher(highest, sample.surface());
    }
    return highest;
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
