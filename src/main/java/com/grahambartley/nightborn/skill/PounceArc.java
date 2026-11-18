package com.grahambartley.nightborn.skill;

import net.minecraft.util.math.Vec3d;

public final class PounceArc {

  static final double GRAVITY = 0.08;
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
    return new Vec3d(
        delta.x / ticks, delta.y / ticks + 0.5 * GRAVITY * (ticks - 1), delta.z / ticks);
  }

  public static boolean caught(final double distance) {
    return distance <= CATCH_REACH;
  }
}
