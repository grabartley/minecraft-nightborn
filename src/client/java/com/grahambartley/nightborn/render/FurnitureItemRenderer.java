package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.decor.FurnitureBlockEntity;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class FurnitureItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

  private final Block block;
  private FurnitureBlockRenderer renderer;
  private FurnitureBlockEntity shown;

  public FurnitureItemRenderer(final Block block) {
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
      this.renderer = new FurnitureBlockRenderer();
      this.shown = new FurnitureBlockEntity(BlockPos.ORIGIN, this.block.getDefaultState());
    }
    this.renderer.render(this.shown, 0.0f, matrices, vertexConsumers, light, overlay);
  }
}
