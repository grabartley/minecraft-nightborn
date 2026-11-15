package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.decor.FurnitureBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class FurnitureBlockRenderer extends GeoBlockRenderer<FurnitureBlockEntity> {

  public FurnitureBlockRenderer(final BlockEntityRendererFactory.Context context) {
    super(new FurnitureModel());
    this.addRenderLayer(
        new AutoGlowingGeoLayer<>(this) {
          @Override
          public void render(
              final MatrixStack poseStack,
              final FurnitureBlockEntity furniture,
              final BakedGeoModel bakedModel,
              final RenderLayer renderType,
              final VertexConsumerProvider bufferSource,
              final VertexConsumer buffer,
              final float partialTick,
              final int packedLight,
              final int packedOverlay) {
            if (furniture.furniture().glows(furniture.getCachedState())) {
              super.render(
                  poseStack,
                  furniture,
                  bakedModel,
                  renderType,
                  bufferSource,
                  buffer,
                  partialTick,
                  packedLight,
                  packedOverlay);
            }
          }
        });
  }
}
