package com.grahambartley.nightborn.render;

import java.util.Optional;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.RotationAxis;

public final class HandItem {

  public static final String RIGHT_BONE = "item_right";
  public static final String LEFT_BONE = "item_left";

  private HandItem() {}

  public static Optional<Arm> arm(final String bone) {
    return switch (bone) {
      case RIGHT_BONE -> Optional.of(Arm.RIGHT);
      case LEFT_BONE -> Optional.of(Arm.LEFT);
      default -> Optional.empty();
    };
  }

  public static <S> S stack(final Arm hand, final Arm mainArm, final S mainHand, final S offHand) {
    return hand == mainArm ? mainHand : offHand;
  }

  public static ModelTransformationMode mode(final Arm hand) {
    return hand == Arm.RIGHT
        ? ModelTransformationMode.THIRD_PERSON_RIGHT_HAND
        : ModelTransformationMode.THIRD_PERSON_LEFT_HAND;
  }

  public static void pose(final MatrixStack poseStack) {
    poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
    poseStack.translate(0.0F, 0.125F, -0.0625F);
  }
}
