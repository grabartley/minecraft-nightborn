package com.grahambartley.nightborn.render;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class GeoBlockItemRenderer<T extends BlockEntity & GeoAnimatable>
    implements BuiltinItemRendererRegistry.DynamicItemRenderer {

  private final Block block;
  private final Supplier<GeoBlockRenderer<T>> rendererFactory;
  private final BiFunction<BlockPos, BlockState, T> entityFactory;
  private final float shiftZ;
  private GeoBlockRenderer<T> renderer;
  private T shown;

  public GeoBlockItemRenderer(
      final Block block,
      final Supplier<GeoBlockRenderer<T>> rendererFactory,
      final BiFunction<BlockPos, BlockState, T> entityFactory,
      final float shiftZ) {
    this.block = block;
    this.rendererFactory = rendererFactory;
    this.entityFactory = entityFactory;
    this.shiftZ = shiftZ;
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
      this.renderer = this.rendererFactory.get();
      this.shown = this.entityFactory.apply(BlockPos.ORIGIN, this.block.getDefaultState());
    }
    matrices.push();
    matrices.translate(0.0F, 0.0F, this.shiftZ);
    this.renderer.render(this.shown, 0.0F, matrices, vertexConsumers, light, overlay);
    matrices.pop();
  }
}
