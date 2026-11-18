package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.batform.WingedForm;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public final class BatFormRenderer
    extends GeoReplacedEntityRenderer<LivingEntity, BatFormAnimatable> {

  public BatFormRenderer(final EntityRendererFactory.Context context) {
    super(
        context,
        new DefaultedEntityGeoModel<>(Identifier.of(Nightborn.MOD_ID, "vampire_bat"), true),
        BatFormAnimatable.INSTANCE);
    this.shadowRadius = VampireBatRenderer.SHADOW_RADIUS;
    addRenderLayer(new AutoGlowingGeoLayer<>(this));
  }

  @Override
  public void preRender(
      final MatrixStack poseStack,
      final BatFormAnimatable animatable,
      final BakedGeoModel model,
      @Nullable final VertexConsumerProvider bufferSource,
      @Nullable final VertexConsumer buffer,
      final boolean isReRender,
      final float partialTick,
      final int packedLight,
      final int packedOverlay,
      final int colour) {
    final float scale = currentEntity == null ? 1.0F : WingedForm.scale(currentEntity);
    scaleWidth = scale;
    scaleHeight = scale;
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
  }
}
