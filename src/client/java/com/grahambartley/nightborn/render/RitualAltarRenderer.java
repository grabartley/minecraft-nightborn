package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.decor.ChaliceBlock;
import com.grahambartley.nightborn.decor.DecorBlocks;
import com.grahambartley.nightborn.ritual.RitualAltarBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class RitualAltarRenderer extends GeoBlockRenderer<RitualAltarBlockEntity> {

  static final String ART = "ritual_altar";
  static final double CHALICE_TOP = 14.0 / 16.0;

  public RitualAltarRenderer(final BlockEntityRendererFactory.Context context) {
    this();
  }

  public RitualAltarRenderer() {
    super(new FixedArtModel<>(ART));
  }

  @Override
  public void renderFinal(
      final MatrixStack matrices,
      final RitualAltarBlockEntity altar,
      final BakedGeoModel model,
      final VertexConsumerProvider vertices,
      final VertexConsumer buffer,
      final float partialTick,
      final int packedLight,
      final int packedOverlay,
      final int colour) {
    altar
        .chalice()
        .ifPresent(
            contents -> {
              matrices.push();
              matrices.translate(0.0, CHALICE_TOP, 0.0);
              MinecraftClient.getInstance()
                  .getBlockRenderManager()
                  .renderBlockAsEntity(
                      ChaliceBlock.with(DecorBlocks.CHALICE.getDefaultState(), contents),
                      matrices,
                      vertices,
                      packedLight,
                      OverlayTexture.DEFAULT_UV);
              matrices.pop();
            });
  }
}
