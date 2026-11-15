package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.mob.VampireBatEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class VampireBatRenderer extends GeoEntityRenderer<VampireBatEntity> {

  static final float SHADOW_RADIUS = 0.25F;

  public VampireBatRenderer(final EntityRendererFactory.Context context) {
    super(
        context,
        new DefaultedEntityGeoModel<>(Identifier.of(Nightborn.MOD_ID, "vampire_bat"), true));
    this.shadowRadius = SHADOW_RADIUS;
    addRenderLayer(new AutoGlowingGeoLayer<>(this));
  }
}
