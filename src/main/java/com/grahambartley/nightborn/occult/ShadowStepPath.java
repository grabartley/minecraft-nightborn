package com.grahambartley.nightborn.occult;

import net.minecraft.util.math.Vec3d;

public final class ShadowStepPath {

  private ShadowStepPath() {}

  public static Vec3d behind(final Vec3d caster, final Vec3d target, final double distance) {
    final Vec3d flat = new Vec3d(target.x - caster.x, 0.0, target.z - caster.z);
    if (flat.lengthSquared() < 1.0E-6) {
      return target;
    }
    return target.add(flat.normalize().multiply(distance));
  }

  public static Vec3d aimBehind(
      final Vec3d caster,
      final double eyeHeight,
      final Vec3d target,
      final double behind,
      final double range) {
    return toward(caster, eyeHeight, behind(caster, target, behind), range);
  }

  public static Vec3d toward(
      final Vec3d caster, final double eyeHeight, final Vec3d spot, final double range) {
    final Vec3d lift = new Vec3d(0.0, eyeHeight, 0.0);
    final Vec3d eye = caster.add(lift);
    final Vec3d wanted = spot.add(lift);
    final Vec3d way = wanted.subtract(eye);
    return way.length() > range ? eye.add(way.normalize().multiply(range)) : wanted;
  }
}
