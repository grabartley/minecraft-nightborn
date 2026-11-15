package com.grahambartley.nightborn.render;

import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;

public class ClothingRenderProvider implements GeoRenderProvider {

  private ClothingRenderer renderer;

  @Override
  public <T extends LivingEntity> BipedEntityModel<?> getGeoArmorRenderer(
      final T wearer,
      final ItemStack stack,
      final EquipmentSlot slot,
      final BipedEntityModel<T> original) {
    if (this.renderer == null) {
      this.renderer = new ClothingRenderer();
    }
    return this.renderer;
  }
}
