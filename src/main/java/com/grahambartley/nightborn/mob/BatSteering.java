package com.grahambartley.nightborn.mob;

import net.minecraft.util.math.Vec3d;

public final class BatSteering {

  static final double SWOOP_SPEED = 0.55;
  static final double TURN = 0.2;

  private BatSteering() {}

  public static Vec3d toward(final Vec3d velocity, final Vec3d offset) {
    if (offset.lengthSquared() < 1.0E-6) {
      return velocity;
    }
    final Vec3d wanted = offset.normalize().multiply(SWOOP_SPEED);
    return velocity.add(wanted.subtract(velocity).multiply(TURN));
  }

  public static float yaw(final Vec3d velocity) {
    return (float) Math.toDegrees(Math.atan2(velocity.z, velocity.x)) - 90.0F;
  }
}
