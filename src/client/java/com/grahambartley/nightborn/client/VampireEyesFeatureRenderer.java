package com.grahambartley.nightborn.client;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.vampire.VisibleStateAttachment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public final class VampireEyesFeatureRenderer
    extends FeatureRenderer<
        AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

  static final RenderLayer EYES =
      RenderLayer.getEyes(Identifier.of(Nightborn.MOD_ID, "textures/entity/vampire_eyes.png"));

  public VampireEyesFeatureRenderer(
      final FeatureRendererContext<
              AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>
          context) {
    super(context);
  }

  @Override
  public void render(
      final MatrixStack matrices,
      final VertexConsumerProvider vertexConsumers,
      final int light,
      final AbstractClientPlayerEntity player,
      final float limbAngle,
      final float limbDistance,
      final float tickDelta,
      final float animationProgress,
      final float headYaw,
      final float headPitch) {
    if (player.isInvisible() || !VisibleStateAttachment.get(player).eyesGlowing()) {
      return;
    }
    getContextModel()
        .render(
            matrices,
            vertexConsumers.getBuffer(EYES),
            LightmapTextureManager.MAX_LIGHT_COORDINATE,
            OverlayTexture.DEFAULT_UV);
  }
}
