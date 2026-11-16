package com.grahambartley.nightborn.render;

import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.cache.object.GeoBone;

public final class BustShape {

  static final String BONE = "bust";

  private BustShape() {}

  public static void size(final MatrixStack poseStack, final GeoBone bone, final float bust) {
    if (!bone.getName().endsWith(BONE)) {
      return;
    }
    final float x = bone.getPivotX() / 16.0F;
    final float y = bone.getPivotY() / 16.0F;
    final float z = bone.getPivotZ() / 16.0F;
    poseStack.translate(x, y, z);
    poseStack.scale(bust, bust, bust);
    poseStack.translate(-x, -y, -z);
  }

  static boolean underPlate(final ArmorDraw chest) {
    return chest == ArmorDraw.VANILLA;
  }
}
