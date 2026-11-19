package com.grahambartley.nightborn.skill;

import net.minecraft.util.math.Vec3d;

public final class AreaRules {

  private AreaRules() {}

  public static boolean inside(final double squaredDistance, final float radius) {
    return squaredDistance <= (double) radius * radius;
  }

  public static double flat(final Vec3d at, final Vec3d centre) {
    final double dx = at.x - centre.x;
    final double dz = at.z - centre.z;
    return dx * dx + dz * dz;
  }

  public static boolean column(
      final Vec3d at, final Vec3d centre, final float radius, final float height) {
    return at.y >= centre.y - 1.0 && at.y <= centre.y + height && inside(flat(at, centre), radius);
  }

  public static boolean lasts(final long now, final long until) {
    return now < until;
  }
}
