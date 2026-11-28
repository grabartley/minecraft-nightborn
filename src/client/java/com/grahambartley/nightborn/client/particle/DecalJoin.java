package com.grahambartley.nightborn.client.particle;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class DecalJoin {

  static final double REACH_SHARE = 0.6;
  static final double GROWTH_SHARE = 0.5;
  static final double LARGEST_HALF_SIZE = 1.0;
  static final double SAME_PLANE = 1.0e-3;

  private DecalJoin() {}

  public static boolean joins(
      final Direction face,
      final Vec3d centre,
      final double halfSize,
      final float wetness,
      final Direction incomingFace,
      final Vec3d incoming) {
    if (wetness < 1.0F || face != incomingFace) {
      return false;
    }
    final Direction.Axis normal = face.getAxis();
    final double planeGap =
        Math.abs(centre.getComponentAlongAxis(normal) - incoming.getComponentAlongAxis(normal));
    return planeGap <= SAME_PLANE
        && centre.squaredDistanceTo(incoming) <= square(halfSize * REACH_SHARE);
  }

  public static double grown(final double halfSize, final double incomingHalfSize) {
    return Math.min(
        LARGEST_HALF_SIZE,
        Math.max(halfSize, Math.sqrt(square(halfSize) + GROWTH_SHARE * square(incomingHalfSize))));
  }

  private static double square(final double value) {
    return value * value;
  }
}
