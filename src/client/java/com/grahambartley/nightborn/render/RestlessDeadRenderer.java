package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.occult.RestlessDeadEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RestlessDeadRenderer extends GeoEntityRenderer<RestlessDeadEntity> {

  static final float SHADOW_RADIUS = 0.5F;

  public RestlessDeadRenderer(final EntityRendererFactory.Context context) {
    super(context, new RestlessDeadModel());
    this.shadowRadius = SHADOW_RADIUS;
  }
}
