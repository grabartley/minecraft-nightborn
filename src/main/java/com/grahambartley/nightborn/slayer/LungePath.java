package com.grahambartley.nightborn.slayer;

import net.minecraft.util.math.Vec3d;

public final class LungePath {

  private LungePath() {}

  public static Vec3d wanted(
      final Vec3d from, final Vec3d target, final double eyeHeight, final double gap) {
    final Vec3d flat = new Vec3d(target.x - from.x, 0.0, target.z - from.z);
    final double length = flat.length();
    if (length <= gap || length < 1.0E-6) {
      return new Vec3d(from.x, from.y + eyeHeight, from.z);
    }
    final Vec3d stop = flat.multiply((length - gap) / length);
    return new Vec3d(from.x + stop.x, target.y + eyeHeight, from.z + stop.z);
  }
}
