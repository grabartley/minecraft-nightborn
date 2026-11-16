package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public final class BatFormRenderer
    extends GeoReplacedEntityRenderer<LivingEntity, BatFormAnimatable> {

  public BatFormRenderer(final EntityRendererFactory.Context context) {
    super(
        context,
        new DefaultedEntityGeoModel<>(Identifier.of(Nightborn.MOD_ID, "vampire_bat"), true),
        BatFormAnimatable.INSTANCE);
    this.shadowRadius = VampireBatRenderer.SHADOW_RADIUS;
    addRenderLayer(new AutoGlowingGeoLayer<>(this));
  }
}
