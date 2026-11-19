package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoObjectRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

final class StatusMarkRenderer extends GeoObjectRenderer<StatusMarkAnimatable> {

  StatusMarkRenderer(final StatusMarkAnimatable animatable, final boolean glows) {
    super(new DefaultedEntityGeoModel<>(Identifier.of(Nightborn.MOD_ID, animatable.rig()), false));
    if (glows) {
      addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
  }

  void draw(
      final MatrixStack matrices,
      final StatusMarkAnimatable animatable,
      final VertexConsumerProvider provider,
      final float width,
      final float height,
      final int light,
      final float tickDelta) {
    withScale(width, height);
    final RenderLayer layer =
        getRenderType(animatable, getTextureLocation(animatable), provider, tickDelta);
    render(matrices, animatable, provider, layer, provider.getBuffer(layer), light, tickDelta);
  }

  @Override
  public RenderLayer getRenderType(
      final StatusMarkAnimatable animatable,
      final Identifier texture,
      @Nullable final VertexConsumerProvider bufferSource,
      final float partialTick) {
    return RenderLayer.getEntityTranslucent(texture);
  }

  @Override
  public void preRender(
      final MatrixStack poseStack,
      final StatusMarkAnimatable animatable,
      final BakedGeoModel model,
      @Nullable final VertexConsumerProvider bufferSource,
      @Nullable final net.minecraft.client.render.VertexConsumer buffer,
      final boolean isReRender,
      final float partialTick,
      final int packedLight,
      final int packedOverlay,
      final int colour) {
    this.objectRenderTranslations = new org.joml.Matrix4f(poseStack.peek().getPositionMatrix());
    scaleModelForRender(
        this.scaleWidth,
        this.scaleHeight,
        poseStack,
        animatable,
        model,
        isReRender,
        partialTick,
        packedLight,
        packedOverlay);
  }
}
