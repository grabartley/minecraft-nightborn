package com.grahambartley.nightborn.client.gore;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class BloodSpray {

  private BloodSpray() {}

  public static Vec3d velocity(final float yawDegrees, final double speed, final double lift) {
    final float angle = yawDegrees * MathHelper.RADIANS_PER_DEGREE;
    return new Vec3d(MathHelper.cos(angle) * speed, lift, MathHelper.sin(angle) * speed);
  }
}
