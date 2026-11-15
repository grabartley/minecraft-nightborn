package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.coffin.CoffinBlock;
import com.grahambartley.nightborn.coffin.CoffinBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;

public class CoffinBlockEntityRenderer implements BlockEntityRenderer<CoffinBlockEntity> {

  private final BlockModelRenderer models;

  public CoffinBlockEntityRenderer(final BlockEntityRendererFactory.Context context) {
    this.models = context.getRenderManager().getModelRenderer();
  }

  @Override
  public void render(
      final CoffinBlockEntity coffin,
      final float tickDelta,
      final MatrixStack matrices,
      final VertexConsumerProvider vertexConsumers,
      final int light,
      final int overlay) {
    final BlockState state = coffin.getCachedState();
    if (!(state.getBlock() instanceof CoffinBlock)) {
      return;
    }
    final BakedModel lid =
        MinecraftClient.getInstance()
            .getBakedModelManager()
            .getModel(CoffinModels.lidOf(state.getBlock()));
    matrices.push();
    CoffinLidPose.apply(
        matrices,
        state.get(CoffinBlock.FACING),
        state.get(CoffinBlock.STANDING),
        coffin.openness(tickDelta));
    this.models.render(
        matrices.peek(),
        vertexConsumers.getBuffer(RenderLayers.getEntityBlockLayer(state, false)),
        state,
        lid,
        1.0F,
        1.0F,
        1.0F,
        light,
        overlay);
    matrices.pop();
  }

  @Override
  public boolean rendersOutsideBoundingBox(final CoffinBlockEntity coffin) {
    return true;
  }
}
