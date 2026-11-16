package com.grahambartley.nightborn.client.guild;

import com.grahambartley.nightborn.guild.HunterLooking;
import com.grahambartley.nightborn.render.HandItemGeoLayer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.LivingEntity;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class HunterRenderer<T extends LivingEntity & GeoEntity & HunterLooking>
    extends GeoEntityRenderer<T> {

  static final float SHADOW_RADIUS = 0.5F;

  public HunterRenderer(final EntityRendererFactory.Context context) {
    super(context, new HunterModel<>());
    this.shadowRadius = SHADOW_RADIUS;
    addRenderLayer(new HandItemGeoLayer<>(this));
  }
}
