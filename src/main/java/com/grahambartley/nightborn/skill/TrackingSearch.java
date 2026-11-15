package com.grahambartley.nightborn.skill;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.util.math.Vec3d;

public final class TrackingSearch {

  private TrackingSearch() {}

  public static <T> Optional<T> nearest(
      final Vec3d from, final List<T> candidates, final Function<T, Vec3d> at, final int range) {
    final double reach = (double) range * range;
    return candidates.stream()
        .filter(candidate -> at.apply(candidate).squaredDistanceTo(from) <= reach)
        .min(Comparator.comparingDouble(candidate -> at.apply(candidate).squaredDistanceTo(from)));
  }
}
