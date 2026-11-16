package com.grahambartley.nightborn.mind;

import net.minecraft.util.math.Vec3d;

public final class Stare {

  static final double MAX_ANGLE_DEGREES = 12.0;

  private Stare() {}

  public static boolean holds(
      final boolean steered,
      final double angleDegrees,
      final double distance,
      final double range,
      final boolean sees) {
    return sees && distance <= range && (steered || angleDegrees <= MAX_ANGLE_DEGREES);
  }

  public static double angleDegrees(final Vec3d look, final Vec3d toward) {
    if (look.lengthSquared() < 1.0E-8 || toward.lengthSquared() < 1.0E-8) {
      return 180.0;
    }
    final double cos = look.normalize().dotProduct(toward.normalize());
    return Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, cos))));
  }
}
