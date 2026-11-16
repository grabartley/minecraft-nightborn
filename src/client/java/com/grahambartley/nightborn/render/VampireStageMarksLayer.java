package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.mob.VampireEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class VampireStageMarksLayer extends GeoRenderLayer<VampireEntity> {

  public VampireStageMarksLayer(final GeoRenderer<VampireEntity> renderer) {
    super(renderer);
  }

  @Override
  public void render(
      final MatrixStack poseStack,
      final VampireEntity vampire,
      final BakedGeoModel bakedModel,
      @Nullable final RenderLayer renderType,
      final VertexConsumerProvider bufferSource,
      @Nullable final VertexConsumer buffer,
      final float partialTick,
      final int packedLight,
      final int packedOverlay) {
    if (vampire.isInvisible()) {
      return;
    }
    vampire
        .stage()
        .look()
        .marksTexture(vampire.rig().asString())
        .ifPresent(
            texture -> {
              final RenderLayer marks = RenderLayer.getEntityTranslucent(texture);
              getRenderer()
                  .reRender(
                      bakedModel,
                      poseStack,
                      bufferSource,
                      vampire,
                      marks,
                      bufferSource.getBuffer(marks),
                      partialTick,
                      packedLight,
                      packedOverlay,
                      0xFFFFFFFF);
            });
  }
}
