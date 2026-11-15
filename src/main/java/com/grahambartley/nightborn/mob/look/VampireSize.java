package com.grahambartley.nightborn.mob.look;

import net.minecraft.entity.EntityDimensions;

public final class VampireSize {

  public static final float BASE_HEIGHT = 1.95F;
  public static final float BASE_WIDTH = 0.6F;

  private VampireSize() {}

  public static float scale(final float height) {
    return clampHeight(height) / BASE_HEIGHT;
  }

  public static float clampWidth(final float width) {
    return Math.max(VampireBuild.MIN_WIDTH, Math.min(VampireBuild.MAX_WIDTH, width));
  }

  public static float clampHeight(final float height) {
    return Math.max(VampireBuild.MIN_HEIGHT, Math.min(VampireBuild.MAX_HEIGHT, height));
  }

  public static EntityDimensions dimensions(
      final EntityDimensions base, final float height, final float width) {
    final float scale = scale(height);
    return base.scaled(clampWidth(width) * scale, scale);
  }
}
