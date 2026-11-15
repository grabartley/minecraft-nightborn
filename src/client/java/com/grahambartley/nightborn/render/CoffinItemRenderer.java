package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.coffin.CoffinBlockEntity;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class CoffinItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

  private final Block block;
  private CoffinRenderer renderer;
  private CoffinBlockEntity shown;

  public CoffinItemRenderer(final Block block) {
    this.block = block;
  }

  @Override
  public void render(
      final ItemStack stack,
      final ModelTransformationMode mode,
      final MatrixStack matrices,
      final VertexConsumerProvider vertexConsumers,
      final int light,
      final int overlay) {
    if (this.renderer == null) {
      this.renderer = new CoffinRenderer();
      this.shown = new CoffinBlockEntity(BlockPos.ORIGIN, this.block.getDefaultState());
    }
    matrices.push();
    matrices.translate(0.0F, 0.0F, -CoffinArt.MODEL_CENTRE_Z);
    this.renderer.render(this.shown, 0.0F, matrices, vertexConsumers, light, overlay);
    matrices.pop();
  }
}
