package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.mob.VampireEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class VampireRenderer extends GeoEntityRenderer<VampireEntity> {

  static final float SHADOW_RADIUS = 0.5F;

  public VampireRenderer(final EntityRendererFactory.Context context) {
    super(context, new VampireModel());
    this.shadowRadius = SHADOW_RADIUS;
    addRenderLayer(new VampireEyesLayer(this));
  }
}
