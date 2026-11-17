package com.grahambartley.nightborn.boss;

import net.minecraft.util.math.Vec3d;

public final class LeapArc {

  private LeapArc() {}

  public static double height(final double lift, final int tick, final int total) {
    if (total <= 0) {
      return 0.0;
    }
    final double t = Math.max(0.0, Math.min(1.0, (double) tick / total));
    return 4.0 * lift * t * (1.0 - t);
  }

  public static Vec3d at(
      final Vec3d from, final Vec3d to, final double lift, final int tick, final int total) {
    if (total <= 0) {
      return to;
    }
    final double t = Math.max(0.0, Math.min(1.0, (double) tick / total));
    return from.lerp(to, t).add(0.0, height(lift, tick, total), 0.0);
  }

  public static Vec3d step(
      final Vec3d from, final Vec3d to, final double lift, final int tick, final int total) {
    return at(from, to, lift, tick + 1, total).subtract(at(from, to, lift, tick, total));
  }
}
