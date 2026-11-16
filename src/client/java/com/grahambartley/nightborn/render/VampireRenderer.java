package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.look.VampireSize;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class VampireRenderer extends GeoEntityRenderer<VampireEntity> {

  static final float SHADOW_RADIUS = 0.5F;

  public VampireRenderer(final EntityRendererFactory.Context context) {
    super(context, new VampireModel());
    this.shadowRadius = SHADOW_RADIUS;
    addRenderLayer(new VampireStageMarksLayer(this));
    addRenderLayer(new VampireEyesLayer(this));
    addRenderLayer(new RigArmorLayer<>(this, this::getAnimatable));
  }

  @Override
  public void scaleModelForRender(
      final float widthScale,
      final float heightScale,
      final MatrixStack poseStack,
      final VampireEntity vampire,
      final BakedGeoModel model,
      final boolean isReRender,
      final float partialTick,
      final int packedLight,
      final int packedOverlay) {
    final float scale = VampireSize.scale(vampire.height());
    super.scaleModelForRender(
        widthScale * scale * VampireSize.clampWidth(vampire.width()),
        heightScale * scale,
        poseStack,
        vampire,
        model,
        isReRender,
        partialTick,
        packedLight,
        packedOverlay);
  }
}
