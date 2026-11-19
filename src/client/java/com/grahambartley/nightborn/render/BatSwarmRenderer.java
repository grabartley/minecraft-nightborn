package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.occult.BatSwarmEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public final class BatSwarmRenderer extends GeoEntityRenderer<BatSwarmEntity> {

  static final Identifier RIG = Identifier.of(Nightborn.MOD_ID, "bat_swarm");

  public BatSwarmRenderer(final EntityRendererFactory.Context context) {
    super(context, new DefaultedEntityGeoModel<>(RIG, false));
    this.shadowRadius = 0.0F;
    addRenderLayer(new AutoGlowingGeoLayer<>(this));
  }
}
