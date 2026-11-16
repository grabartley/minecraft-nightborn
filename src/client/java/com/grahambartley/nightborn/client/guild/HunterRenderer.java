package com.grahambartley.nightborn.client.guild;

import com.grahambartley.nightborn.guild.HunterLooking;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class HunterRenderer<T extends LivingEntity & GeoEntity & HunterLooking>
    extends GeoEntityRenderer<T> {

  static final float SHADOW_RADIUS = 0.5F;
  static final String RIGHT_ITEM = "right_item";

  public HunterRenderer(final EntityRendererFactory.Context context) {
    super(context, new HunterModel<>());
    this.shadowRadius = SHADOW_RADIUS;
    addRenderLayer(
        new BlockAndItemGeoLayer<>(
            this,
            (bone, hunter) ->
                RIGHT_ITEM.equals(bone.getName()) ? hunter.getMainHandStack() : ItemStack.EMPTY,
            (bone, hunter) -> null));
  }
}
