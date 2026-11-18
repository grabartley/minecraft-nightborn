package com.grahambartley.nightborn.skill;

import net.minecraft.util.math.Vec3d;

public final class PounceArc {

  static final double GRAVITY = 0.08;
  static final double AIR_DRAG = 0.91;
  static final double FALL_DRAG = 0.98;
  static final double TICKS_PER_BLOCK = 1.1;
  static final int FEWEST_TICKS = 6;
  static final double CATCH_REACH = 2.0;

  private PounceArc() {}

  public static int flightTicks(final double distance) {
    return Math.max(FEWEST_TICKS, (int) Math.ceil(distance * TICKS_PER_BLOCK));
  }

  public static Vec3d launch(final Vec3d from, final Vec3d to) {
    final Vec3d delta = to.subtract(from);
    final int ticks = flightTicks(delta.horizontalLength());
    final double carry = (1.0 - Math.pow(AIR_DRAG, ticks)) / (1.0 - AIR_DRAG);
    double lift = 0.0;
    double drop = 0.0;
    double velocityShare = 1.0;
    double fall = 0.0;
    for (int i = 0; i < ticks; i++) {
      lift += velocityShare;
      drop += fall;
      velocityShare *= FALL_DRAG;
      fall = (fall - GRAVITY) * FALL_DRAG;
    }
    return new Vec3d(delta.x / carry, (delta.y - drop) / lift, delta.z / carry);
  }

  public static boolean caught(final double distance) {
    return distance <= CATCH_REACH;
  }
}
