package com.grahambartley.nightborn.render;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public final class WingedFormRenderer
    extends GeoReplacedEntityRenderer<LivingEntity, WingedFormAnimatable> {

  static final float SHADOW_RADIUS = 0.8F;

  public WingedFormRenderer(final EntityRendererFactory.Context context, final Identifier rig) {
    super(context, new DefaultedEntityGeoModel<>(rig, true), new WingedFormAnimatable());
    this.shadowRadius = SHADOW_RADIUS;
    addRenderLayer(new AutoGlowingGeoLayer<>(this));
  }
}
