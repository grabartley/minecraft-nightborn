package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.ritual.RitualAltarBlock;
import com.grahambartley.nightborn.ritual.RitualAltarBlockEntity;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class RitualAltarRenderer extends GeoBlockRenderer<RitualAltarBlockEntity> {

  static final String ART = "ritual_altar";

  public RitualAltarRenderer(final BlockEntityRendererFactory.Context context) {
    this();
  }

  public RitualAltarRenderer() {
    super(new FixedArtModel<>(ART));
  }

  @Override
  public void renderFinal(
      final MatrixStack matrices,
      final RitualAltarBlockEntity altar,
      final BakedGeoModel model,
      final VertexConsumerProvider vertices,
      final VertexConsumer buffer,
      final float partialTick,
      final int packedLight,
      final int packedOverlay,
      final int colour) {
    final List<ItemStack> relics = altar.relics();
    if (relics.isEmpty()) {
      return;
    }
    matrices.push();
    matrices.translate(0.5, 0.0, 0.5);
    matrices.multiply(
        RotationAxis.POSITIVE_Y.rotationDegrees(
            180.0F - altar.getCachedState().get(RitualAltarBlock.FACING).asRotation()));
    for (int slot = 0; slot < relics.size(); slot++) {
      final Vec3d at = AltarRelicSlots.offset(slot, relics.size());
      matrices.push();
      matrices.translate(at.x, at.y, at.z);
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
      matrices.scale(AltarRelicSlots.SCALE, AltarRelicSlots.SCALE, AltarRelicSlots.SCALE);
      MinecraftClient.getInstance()
          .getItemRenderer()
          .renderItem(
              relics.get(slot),
              ModelTransformationMode.FIXED,
              packedLight,
              OverlayTexture.DEFAULT_UV,
              matrices,
              vertices,
              altar.getWorld(),
              (int) altar.getPos().asLong() + slot);
      matrices.pop();
    }
    matrices.pop();
  }
}
