package com.grahambartley.nightborn.mob.brain;

import java.util.List;
import net.minecraft.util.math.Vec3d;

public record Route(double gapAhead, double detour, double tripLength) {

  public static final Route UNKNOWN = new Route(Double.NaN, Double.NaN, Double.NaN);

  static final double SHORTEST_TRIP = 1.0;

  public static Route of(
      final Vec3d from,
      final List<Vec3d> nodes,
      final Vec3d goal,
      final boolean reaches,
      final double searchRange) {
    final double trip = from.distanceTo(goal);
    if (!reaches && trip > searchRange) {
      return new Route(Double.NaN, Double.NaN, trip);
    }
    if (!reaches) {
      return new Route(1.0, Double.NaN, trip);
    }
    double walk = 0.0;
    Vec3d at = from;
    for (final Vec3d node : nodes) {
      walk += at.distanceTo(node);
      at = node;
    }
    return new Route(0.0, trip < SHORTEST_TRIP ? 1.0 : Math.max(1.0, walk / trip), trip);
  }
}
