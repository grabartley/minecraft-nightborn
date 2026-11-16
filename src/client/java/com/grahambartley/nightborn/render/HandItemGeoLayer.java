package com.grahambartley.nightborn.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public final class HandItemGeoLayer<T extends LivingEntity & GeoAnimatable>
    extends BlockAndItemGeoLayer<T> {

  public HandItemGeoLayer(final GeoRenderer<T> renderer) {
    super(renderer);
  }

  @Nullable
  @Override
  protected ItemStack getStackForBone(final GeoBone bone, final T entity) {
    return HandItem.arm(bone.getName())
        .map(
            hand ->
                HandItem.stack(
                    hand, entity.getMainArm(), entity.getMainHandStack(), entity.getOffHandStack()))
        .filter(stack -> !stack.isEmpty())
        .orElse(null);
  }

  @Override
  protected ModelTransformationMode getTransformTypeForStack(
      final GeoBone bone, final ItemStack stack, final T entity) {
    return HandItem.arm(bone.getName()).map(HandItem::mode).orElse(ModelTransformationMode.NONE);
  }

  @Override
  protected void renderStackForBone(
      final MatrixStack poseStack,
      final GeoBone bone,
      final ItemStack stack,
      final T entity,
      final VertexConsumerProvider bufferSource,
      final float partialTick,
      final int packedLight,
      final int packedOverlay) {
    HandItem.pose(poseStack);
    MinecraftClient.getInstance()
        .getItemRenderer()
        .renderItem(
            entity,
            stack,
            getTransformTypeForStack(bone, stack, entity),
            HandItem.arm(bone.getName()).orElse(Arm.RIGHT) == Arm.LEFT,
            poseStack,
            bufferSource,
            entity.getWorld(),
            packedLight,
            packedOverlay,
            entity.getId());
  }
}
