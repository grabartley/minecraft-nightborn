package com.grahambartley.nightborn.occult;

import net.minecraft.util.math.Vec3d;

public final class WispTrail {

  static final int EVERY_TICKS = 3;
  static final int FADING_EVERY_TICKS = 1;
  static final double SPREAD = 0.25;
  static final double HEIGHT = 0.25;
  static final double BEHIND = 0.35;
  public static final double DRIFT_UP = 0.012;

  private WispTrail() {}

  public static boolean sheds(final int age, final boolean fading) {
    return age % (fading ? FADING_EVERY_TICKS : EVERY_TICKS) == 0;
  }

  public static Vec3d spawnAt(
      final Vec3d feet, final float bodyYaw, final double rollX, final double rollZ) {
    final double yaw = Math.toRadians(bodyYaw);
    return feet.add(
        Math.sin(yaw) * BEHIND + offset(rollX), HEIGHT, -Math.cos(yaw) * BEHIND + offset(rollZ));
  }

  static double offset(final double roll) {
    return (roll * 2.0 - 1.0) * SPREAD;
  }
}
