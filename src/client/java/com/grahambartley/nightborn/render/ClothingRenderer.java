package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.clothing.ClothingCut;
import com.grahambartley.nightborn.clothing.ClothingItem;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.look.Figure;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.specialty.DyeableGeoArmorRenderer;
import software.bernie.geckolib.util.Color;

public class ClothingRenderer extends DyeableGeoArmorRenderer<ClothingItem> {

  private final ClothingModel clothing;
  private boolean drawnByPart;

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
    this.clothing.fit(cut(entity));
    this.drawnByPart = false;
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
  public void applyBoneVisibilityByPart(
      final EquipmentSlot slot, final ModelPart part, final BipedEntityModel<?> model) {
    super.applyBoneVisibilityByPart(slot, part, model);
    this.drawnByPart = true;
  }

  @Override
  protected void applyBoneVisibilityBySlot(final EquipmentSlot slot) {
    if (!this.drawnByPart) {
      super.applyBoneVisibilityBySlot(slot);
    }
  }

  @Override
  protected void applyBaseTransformations(final BipedEntityModel<?> baseModel) {
    super.applyBaseTransformations(baseModel);
    BootPlacement.RIGHT.place(this.rightLeg, this.rightBoot, baseModel.rightLeg);
    BootPlacement.LEFT.place(this.leftLeg, this.leftBoot, baseModel.leftLeg);
  }

  @Override
  public void renderRecursively(
      final MatrixStack poseStack,
      final ClothingItem animatable,
      final GeoBone bone,
      final RenderLayer renderType,
      final VertexConsumerProvider bufferSource,
      final VertexConsumer buffer,
      final boolean isReRender,
      final float partialTick,
      final int packedLight,
      final int packedOverlay,
      final int colour) {
    poseStack.push();
    BustShape.size(
        poseStack, bone, this.currentEntity instanceof Figure figure ? figure.bust() : Figure.FLAT);
    super.renderRecursively(
        poseStack,
        animatable,
        bone,
        renderType,
        bufferSource,
        buffer,
        isReRender,
        partialTick,
        packedLight,
        packedOverlay,
        colour);
    poseStack.pop();
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

  private static ClothingCut cut(final Entity entity) {
    if (entity instanceof VampireEntity vampire) {
      return vampire.rig().cut();
    }
    return ClothingCut.fitting(
        entity instanceof AbstractClientPlayerEntity player
            && player.getSkinTextures().model() == SkinTextures.Model.SLIM);
  }
}
