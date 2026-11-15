package com.grahambartley.nightborn.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

public final class CoffinStance {

  static final float STOOD_UP_DEGREES = 90.0F;
  private static final float HALF_BLOCK = 0.5F;

  private CoffinStance() {}

  public static void apply(final MatrixStack matrices, final boolean standing) {
    if (!standing) {
      return;
    }
    matrices.translate(0.0F, HALF_BLOCK, 0.0F);
    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(STOOD_UP_DEGREES));
    matrices.translate(0.0F, -HALF_BLOCK, 0.0F);
  }
}
