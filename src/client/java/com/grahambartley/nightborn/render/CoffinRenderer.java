package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.coffin.CoffinBlock;
import com.grahambartley.nightborn.coffin.CoffinBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class CoffinRenderer extends GeoBlockRenderer<CoffinBlockEntity> {

  public CoffinRenderer(final BlockEntityRendererFactory.Context context) {
    this();
  }

  public CoffinRenderer() {
    super(new CoffinModel());
  }

  @Override
  protected void rotateBlock(final Direction facing, final MatrixStack matrices) {
    super.rotateBlock(facing, matrices);
    CoffinStance.apply(matrices, CoffinBlock.isStanding(this.animatable.getCachedState()));
  }

  @Override
  public void renderRecursively(
      final MatrixStack matrices,
      final CoffinBlockEntity coffin,
      final GeoBone bone,
      final RenderLayer renderType,
      final VertexConsumerProvider bufferSource,
      final VertexConsumer buffer,
      final boolean isReRender,
      final float partialTick,
      final int packedLight,
      final int packedOverlay,
      final int colour) {
    final VertexConsumer material =
        CoffinArt.texture(bone.getName(), this.getTextureLocation(coffin))
            .map(texture -> bufferSource.getBuffer(RenderLayer.getEntityCutoutNoCull(texture)))
            .orElse(buffer);
    super.renderRecursively(
        matrices,
        coffin,
        bone,
        renderType,
        bufferSource,
        material,
        isReRender,
        partialTick,
        packedLight,
        packedOverlay,
        colour);
  }

  @Override
  public boolean rendersOutsideBoundingBox(final CoffinBlockEntity coffin) {
    return true;
  }
}
