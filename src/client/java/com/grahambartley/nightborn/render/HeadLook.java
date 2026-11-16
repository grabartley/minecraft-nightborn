package com.grahambartley.nightborn.render;

import net.minecraft.util.math.MathHelper;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.data.EntityModelData;

public final class HeadLook {

  private HeadLook() {}

  public static void apply(final GeoBone head, final EntityModelData data) {
    apply(head, data.headPitch(), data.netHeadYaw());
  }

  static void apply(final GeoBone head, final float pitchDegrees, final float yawDegrees) {
    head.setRotX(head.getRotX() + pitchDegrees * MathHelper.RADIANS_PER_DEGREE);
    head.setRotY(head.getRotY() + yawDegrees * MathHelper.RADIANS_PER_DEGREE);
    head.resetStateChanges();
  }
}
