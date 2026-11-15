package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.clothing.ClothingCut;
import com.grahambartley.nightborn.clothing.ClothingItem;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.specialty.DyeableGeoArmorRenderer;
import software.bernie.geckolib.util.Color;

public class ClothingRenderer extends DyeableGeoArmorRenderer<ClothingItem> {

  private final ClothingModel clothing;

  public ClothingRenderer() {
    this(new ClothingModel());
  }

  private ClothingRenderer(final ClothingModel model) {
    super(model);
    this.clothing = model;
  }

  @Override
  public void prepForRender(
      final Entity entity,
      final ItemStack stack,
      final EquipmentSlot slot,
      final BipedEntityModel<?> baseModel,
      final VertexConsumerProvider bufferSource,
      final float partialTick,
      final float limbSwing,
      final float limbSwingAmount,
      final float netHeadYaw,
      final float headPitch) {
    this.clothing.fit(ClothingCut.fitting(slimBody(entity)));
    super.prepForRender(
        entity,
        stack,
        slot,
        baseModel,
        bufferSource,
        partialTick,
        limbSwing,
        limbSwingAmount,
        netHeadYaw,
        headPitch);
  }

  @Override
  public Color getRenderColor(
      final ClothingItem item, final float partialTick, final int packedLight) {
    return Color.WHITE;
  }

  @Override
  protected boolean isBoneDyeable(final GeoBone bone) {
    return ClothingArt.isDyed(bone.getName());
  }

  @Override
  protected Color getColorForBone(final GeoBone bone) {
    return Color.ofOpaque(((ClothingItem) this.currentStack.getItem()).color(this.currentStack));
  }

  private static boolean slimBody(final Entity entity) {
    return entity instanceof AbstractClientPlayerEntity player
        && player.getSkinTextures().model() == SkinTextures.Model.SLIM;
  }
}
