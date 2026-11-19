package com.grahambartley.nightborn.client.sun;

import com.grahambartley.nightborn.sun.ParasolItem;
import com.grahambartley.nightborn.sun.Parasols;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class ParasolRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

  static final String OPEN_BONE = "open";
  static final String CLOSED_BONE = "closed";

  private Drawn drawn;

  static boolean hides(final String bone, final boolean open) {
    return OPEN_BONE.equals(bone) ? !open : CLOSED_BONE.equals(bone) && open;
  }

  @Override
  public void render(
      final ItemStack stack,
      final ModelTransformationMode mode,
      final MatrixStack matrices,
      final VertexConsumerProvider vertexConsumers,
      final int light,
      final int overlay) {
    if (this.drawn == null) {
      this.drawn = new Drawn();
    }
    this.drawn.render(stack, mode, matrices, vertexConsumers, light, overlay);
  }

  private static final class Drawn extends GeoItemRenderer<ParasolItem> {

    Drawn() {
      super(new ParasolModel());
    }

    @Override
    public void preRender(
        final MatrixStack poseStack,
        final ParasolItem animatable,
        final BakedGeoModel model,
        final VertexConsumerProvider bufferSource,
        final VertexConsumer buffer,
        final boolean isReRender,
        final float partialTick,
        final int packedLight,
        final int packedOverlay,
        final int colour) {
      super.preRender(
          poseStack,
          animatable,
          model,
          bufferSource,
          buffer,
          isReRender,
          partialTick,
          packedLight,
          packedOverlay,
          colour);
      if (isReRender) {
        return;
      }
      final boolean open = Parasols.isOpen(this.currentItemStack);
      model.getBone(OPEN_BONE).ifPresent(bone -> bone.setHidden(hides(OPEN_BONE, open)));
      model.getBone(CLOSED_BONE).ifPresent(bone -> bone.setHidden(hides(CLOSED_BONE, open)));
      poseStack.translate(0.0F, -0.51F, 0.0F);
    }
  }
}
