package com.grahambartley.nightborn.faith;

import net.minecraft.util.math.Vec3d;

public final class BladePoints {

  static final double SIDE = 0.37;
  static final double HAND_HEIGHT = 0.42;
  static final double HAND_REACH = 0.15;
  static final double REACH = 0.75;
  static final double RISE = 0.05;

  private BladePoints() {}

  public static Vec3d along(
      final float bodyYaw, final boolean rightArm, final double height, final double t) {
    final double yaw = Math.toRadians(bodyYaw);
    final double forwardX = -Math.sin(yaw);
    final double forwardZ = Math.cos(yaw);
    final double side = rightArm ? 1.0 : -1.0;
    return new Vec3d(
        -forwardZ * SIDE * side + forwardX * (HAND_REACH + REACH * t),
        height * HAND_HEIGHT + RISE * t,
        forwardX * SIDE * side + forwardZ * (HAND_REACH + REACH * t));
  }
}
