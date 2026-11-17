package com.grahambartley.nightborn.mob.look;

import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;

public final class Weighted {

  public static final int MAX_WEIGHT = 1000;

  private Weighted() {}

  public static <T> Optional<T> pick(
      final List<T> entries, final ToIntFunction<T> weight, final double roll) {
    final long total = entries.stream().mapToLong(e -> Math.max(0, weight.applyAsInt(e))).sum();
    if (total <= 0) {
      return Optional.empty();
    }
    final double target = Math.max(0.0, Math.min(roll, Math.nextDown(1.0))) * total;
    long reached = 0;
    for (final T entry : entries) {
      reached += Math.max(0, weight.applyAsInt(entry));
      if (target < reached) {
        return Optional.of(entry);
      }
    }
    return Optional.of(entries.getLast());
  }
}
