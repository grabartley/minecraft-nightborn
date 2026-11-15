package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.vampire.VisibleStateAttachment;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class VampireEyesLayer extends GeoRenderLayer<VampireEntity> {

  public VampireEyesLayer(final GeoRenderer<VampireEntity> renderer) {
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
    if (vampire.isInvisible() || !VisibleStateAttachment.get(vampire).eyesGlowing()) {
      return;
    }
    final RenderLayer eyes = RenderLayer.getEyes(vampire.rig().eyes());
    getRenderer()
        .reRender(
            bakedModel,
            poseStack,
            bufferSource,
            vampire,
            eyes,
            bufferSource.getBuffer(eyes),
            partialTick,
            LightmapTextureManager.MAX_LIGHT_COORDINATE,
            packedOverlay,
            0xFFFFFFFF);
  }
}
