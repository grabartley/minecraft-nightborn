package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.vampire.VisibleStateAttachment;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public final class VampirePlayerEyesLayer extends GeoRenderLayer<VampirePlayerAnimatable> {

  static final RenderLayer EYES =
      RenderLayer.getEyes(Identifier.of(Nightborn.MOD_ID, "textures/entity/vampire_eyes.png"));

  private final VampirePlayerRenderer players;

  public VampirePlayerEyesLayer(final VampirePlayerRenderer renderer) {
    super(renderer);
    this.players = renderer;
  }

  @Override
  public void render(
      final MatrixStack poseStack,
      final VampirePlayerAnimatable animatable,
      final BakedGeoModel bakedModel,
      @Nullable final RenderLayer renderType,
      final VertexConsumerProvider bufferSource,
      @Nullable final VertexConsumer buffer,
      final float partialTick,
      final int packedLight,
      final int packedOverlay) {
    final var player = players.getCurrentEntity();
    if (player == null
        || player.isInvisible()
        || !VisibleStateAttachment.get(player).eyesGlowing()) {
      return;
    }
    getRenderer()
        .reRender(
            bakedModel,
            poseStack,
            bufferSource,
            animatable,
            EYES,
            bufferSource.getBuffer(EYES),
            partialTick,
            LightmapTextureManager.MAX_LIGHT_COORDINATE,
            packedOverlay,
            0xFFFFFFFF);
  }
}
