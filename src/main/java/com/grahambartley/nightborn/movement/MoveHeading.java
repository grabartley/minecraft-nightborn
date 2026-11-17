package com.grahambartley.nightborn.movement;

import java.util.Optional;

public record MoveHeading(double x, double z) {

  static final double STILL = 1.0E-4;

  public static Optional<MoveHeading> of(
      final double strafe, final double forward, final float yawDegrees) {
    final double length = Math.sqrt(strafe * strafe + forward * forward);
    if (length < STILL) {
      return Optional.empty();
    }
    final double yaw = Math.toRadians(yawDegrees);
    final double sin = Math.sin(yaw);
    final double cos = Math.cos(yaw);
    return Optional.of(
        new MoveHeading(
            (strafe * cos - forward * sin) / length, (forward * cos + strafe * sin) / length));
  }
}
