package com.grahambartley.nightborn.render;

import net.minecraft.util.math.Vec3d;

public final class AltarRelicSlots {

  static final double TOP = 14.5 / 16.0;
  static final double RADIUS = 4.5 / 16.0;
  static final float SCALE = 0.3F;

  private AltarRelicSlots() {}

  public static Vec3d offset(final int slot, final int count) {
    final double angle = Math.PI / 4.0 + 2.0 * Math.PI * slot / Math.max(1, count);
    return new Vec3d(Math.cos(angle) * RADIUS, TOP, Math.sin(angle) * RADIUS);
  }
}
