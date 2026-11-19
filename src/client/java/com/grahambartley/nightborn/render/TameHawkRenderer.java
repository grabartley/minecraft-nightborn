package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.inquisitor.TameHawkEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class TameHawkRenderer extends GeoEntityRenderer<TameHawkEntity> {

  static final float SHADOW_RADIUS = 0.25F;

  public TameHawkRenderer(final EntityRendererFactory.Context context) {
    super(
        context, new DefaultedEntityGeoModel<>(Identifier.of(Nightborn.MOD_ID, "tame_hawk"), true));
    this.shadowRadius = SHADOW_RADIUS;
  }
}
