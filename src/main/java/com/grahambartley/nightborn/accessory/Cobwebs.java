package com.grahambartley.nightborn.accessory;

import net.minecraft.util.math.Vec3d;

public final class Cobwebs {

  private Cobwebs() {}

  public static Vec3d slowing(final Vec3d vanilla, final double slow) {
    final double share = Math.max(0.0, Math.min(1.0, slow));
    return new Vec3d(
        1.0 - (1.0 - vanilla.x) * share,
        1.0 - (1.0 - vanilla.y) * share,
        1.0 - (1.0 - vanilla.z) * share);
  }
}
