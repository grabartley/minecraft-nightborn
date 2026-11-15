package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.client.vomit.VomitFx;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;

public final class VampirePlayerRenderer
    extends GeoReplacedEntityRenderer<AbstractClientPlayerEntity, VampirePlayerAnimatable> {

  static final float SHADOW_RADIUS = 0.5F;
  static final Vector3f MOUTH = new Vector3f(0.0F, 26.0F / 16.0F, -4.2F / 16.0F);

  public VampirePlayerRenderer(final EntityRendererFactory.Context context, final boolean slim) {
    super(context, new VampirePlayerModel(slim), VampirePlayerAnimatable.INSTANCE);
    this.shadowRadius = SHADOW_RADIUS;
    addRenderLayer(new VampirePlayerEyesLayer(this));
    addRenderLayer(new VampirePlayerHeldItemsLayer(this));
    addRenderLayer(new VampirePlayerArmorLayer(this));
  }

  @Override
  public Identifier getTextureLocation(final VampirePlayerAnimatable animatable) {
    return getCurrentEntity() == null
        ? super.getTextureLocation(animatable)
        : getCurrentEntity().getSkinTextures().texture();
  }

  @Override
  public void postRender(
      final MatrixStack poseStack,
      final VampirePlayerAnimatable animatable,
      final BakedGeoModel model,
      final VertexConsumerProvider bufferSource,
      final VertexConsumer buffer,
      final boolean isReRender,
      final float partialTick,
      final int packedLight,
      final int packedOverlay,
      final int colour) {
    if (!isReRender && getCurrentEntity() != null) {
      model
          .getBone("head")
          .map(GeoBone::getWorldSpaceMatrix)
          .ifPresent(
              matrix ->
                  VomitFx.mouth(
                      getCurrentEntity(),
                      new Vec3d(matrix.transformPosition(new Vector3f(MOUTH)))));
    }
    super.postRender(
        poseStack,
        animatable,
        model,
        bufferSource,
        buffer,
        isReRender,
        partialTick,
        packedLight,
        packedOverlay,
        colour);
  }
}
