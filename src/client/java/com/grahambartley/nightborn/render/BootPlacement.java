package com.grahambartley.nightborn.render;

import net.minecraft.client.model.ModelPart;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.util.RenderUtil;

enum BootPlacement {
  RIGHT(2),
  LEFT(-2);

  private static final float HIP_HEIGHT = 12;

  private final float hipOffset;

  BootPlacement(final float hipOffset) {
    this.hipOffset = hipOffset;
  }

  void place(final GeoBone leg, final GeoBone boot, final ModelPart part) {
    if (leg != null || boot == null) {
      return;
    }
    RenderUtil.matchModelPartRot(part, boot);
    boot.updatePosition(part.pivotX + hipOffset, HIP_HEIGHT - part.pivotY, part.pivotZ);
  }
}
