package com.grahambartley.nightborn.client.occult;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.occult.GhostlyFamiliarEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GhostlyFamiliarRenderer extends GeoEntityRenderer<GhostlyFamiliarEntity> {

  static final Identifier MODEL = Identifier.of(Nightborn.MOD_ID, "ghostly_familiar");
  static final float SHADOW_RADIUS = 0.0F;

  public GhostlyFamiliarRenderer(final EntityRendererFactory.Context context) {
    super(context, new DefaultedEntityGeoModel<>(MODEL, true));
    this.shadowRadius = SHADOW_RADIUS;
  }

  @Override
  public RenderLayer getRenderType(
      final GhostlyFamiliarEntity familiar,
      final Identifier texture,
      final VertexConsumerProvider buffers,
      final float partialTick) {
    return RenderLayer.getEntityTranslucent(texture);
  }
}
