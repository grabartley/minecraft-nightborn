package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.house.crest.CrestBannerBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class CrestDyeLayer extends GeoRenderLayer<CrestBannerBlockEntity> {

  public CrestDyeLayer(final GeoRenderer<CrestBannerBlockEntity> renderer) {
    super(renderer);
  }

  @Override
  public void render(
      final MatrixStack poseStack,
      final CrestBannerBlockEntity banner,
      final BakedGeoModel bakedModel,
      @Nullable final RenderLayer renderType,
      final VertexConsumerProvider bufferSource,
      @Nullable final VertexConsumer buffer,
      final float partialTick,
      final int packedLight,
      final int packedOverlay) {
    banner
        .crest()
        .ifPresent(
            crest ->
                CrestDyes.of(crest)
                    .forEach(
                        dye -> {
                          final RenderLayer layer =
                              RenderLayer.getEntityCutoutNoCull(dye.texture());
                          getRenderer()
                              .reRender(
                                  bakedModel,
                                  poseStack,
                                  bufferSource,
                                  banner,
                                  layer,
                                  bufferSource.getBuffer(layer),
                                  partialTick,
                                  packedLight,
                                  packedOverlay,
                                  dye.colour());
                        }));
  }
}
