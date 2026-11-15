package com.grahambartley.nightborn.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

public final class CoffinLidPose {

  static final float OPEN_DEGREES = 100.0F;
  static final float HINGE_X = 1.0F / 16.0F;
  static final float HINGE_Y = 8.0F / 16.0F;
  private static final float AUTHORED_FACING = Direction.NORTH.asRotation();

  private CoffinLidPose() {}

  public static float angle(final float openness) {
    final float closing = 1.0F - openness;
    return (1.0F - closing * closing * closing) * OPEN_DEGREES;
  }

  public static void apply(
      final MatrixStack matrices, final Direction facing, final float openness) {
    matrices.translate(0.5F, 0.0F, 0.5F);
    matrices.multiply(
        RotationAxis.POSITIVE_Y.rotationDegrees(AUTHORED_FACING - facing.asRotation()));
    matrices.translate(-0.5F, 0.0F, -0.5F);
    matrices.translate(HINGE_X, HINGE_Y, 0.0F);
    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle(openness)));
    matrices.translate(-HINGE_X, -HINGE_Y, 0.0F);
  }
}
