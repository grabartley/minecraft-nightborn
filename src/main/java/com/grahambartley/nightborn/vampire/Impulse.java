package com.grahambartley.nightborn.vampire;

import net.minecraft.util.math.Vec3d;

public final class Impulse {

  private Impulse() {}

  public static Vec3d ahead(final Vec3d toward) {
    final Vec3d flat = new Vec3d(toward.x, 0.0, toward.z);
    return flat.lengthSquared() < 1.0E-6 ? Vec3d.ZERO : flat.normalize();
  }

  public static Vec3d push(final Vec3d heading, final double push, final double lift) {
    return heading.multiply(push).add(0.0, lift, 0.0);
  }
}
