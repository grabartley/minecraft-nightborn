package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.house.crest.CrestBannerBlockEntity;
import com.grahambartley.nightborn.house.crest.CrestBanners;
import java.util.Optional;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class CrestBannerItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

  static final float LIFT = 1.25F;

  private CrestBannerRenderer renderer;
  private CrestBannerBlockEntity shown;

  @Override
  public void render(
      final ItemStack stack,
      final ModelTransformationMode mode,
      final MatrixStack matrices,
      final VertexConsumerProvider vertexConsumers,
      final int light,
      final int overlay) {
    if (this.renderer == null) {
      this.renderer = new CrestBannerRenderer();
      this.shown =
          new CrestBannerBlockEntity(BlockPos.ORIGIN, CrestBanners.BLOCK.getDefaultState());
    }
    this.shown.preview(Optional.ofNullable(stack.get(CrestBanners.CREST)));
    matrices.push();
    matrices.translate(0.0F, LIFT, 0.0F);
    this.renderer.render(this.shown, 0.0F, matrices, vertexConsumers, light, overlay);
    matrices.pop();
  }
}
