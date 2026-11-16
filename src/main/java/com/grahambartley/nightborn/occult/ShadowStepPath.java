package com.grahambartley.nightborn.occult;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.Vec3d;

public final class ShadowStepPath {

  static final double STEP = 0.5;
  static final double MIN_TRAVEL = 2.0;

  private ShadowStepPath() {}

  public static Vec3d behind(final Vec3d caster, final Vec3d target, final double distance) {
    final Vec3d flat = new Vec3d(target.x - caster.x, 0.0, target.z - caster.z);
    if (flat.lengthSquared() < 1.0E-6) {
      return target;
    }
    return target.add(flat.normalize().multiply(distance));
  }

  public static List<Vec3d> candidates(final Vec3d from, final Vec3d to) {
    final List<Vec3d> points = new ArrayList<>();
    final Vec3d way = to.subtract(from);
    final double length = way.length();
    if (length < MIN_TRAVEL) {
      return points;
    }
    final Vec3d unit = way.multiply(1.0 / length);
    for (double back = 0.0; length - back >= MIN_TRAVEL; back += STEP) {
      points.add(to.subtract(unit.multiply(back)));
    }
    return points;
  }
}
