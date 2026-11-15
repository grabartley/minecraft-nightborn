package com.grahambartley.nightborn.client.anim;

import java.util.ArrayList;
import java.util.List;

public final class Heaves {

  private Heaves() {}

  public static List<Double> peaks(
      final List<Double> times, final List<Double> degrees, final double deeperThan) {
    final List<Double> peaks = new ArrayList<>();
    for (int i = 0; i < degrees.size(); i++) {
      final double depth = Math.abs(degrees.get(i));
      final double before = i == 0 ? 0.0 : Math.abs(degrees.get(i - 1));
      final double after = i == degrees.size() - 1 ? 0.0 : Math.abs(degrees.get(i + 1));
      if (depth > deeperThan && depth > before && depth >= after) {
        peaks.add(times.get(i));
      }
    }
    return peaks.isEmpty() ? List.of(0.0) : peaks;
  }

  public static List<Double> crossed(
      final List<Double> heaves, final double before, final double now) {
    if (Double.isNaN(now)) {
      return List.of();
    }
    return heaves.stream()
        .filter(heave -> (Double.isNaN(before) || before < heave) && now >= heave)
        .toList();
  }
}
