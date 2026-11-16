package com.grahambartley.nightborn.client.guild;

import com.grahambartley.nightborn.guild.HunterLooking;
import com.grahambartley.nightborn.render.BustShape;
import com.grahambartley.nightborn.render.HandItemGeoLayer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class HunterRenderer<T extends LivingEntity & GeoEntity & HunterLooking>
    extends GeoEntityRenderer<T> {

  static final float SHADOW_RADIUS = 0.5F;

  public HunterRenderer(final EntityRendererFactory.Context context) {
    super(context, new HunterModel<>());
    this.shadowRadius = SHADOW_RADIUS;
    addRenderLayer(new HandItemGeoLayer<>(this));
  }

  @Override
  public void renderRecursively(
      final MatrixStack poseStack,
      final T animatable,
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
    BustShape.size(poseStack, bone, animatable.bust());
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
}
