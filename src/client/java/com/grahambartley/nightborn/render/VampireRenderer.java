package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.client.sight.UnseenBodies;
import com.grahambartley.nightborn.gear.StakedFall;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.look.VampireSize;
import com.grahambartley.nightborn.occult.MistForm;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class VampireRenderer extends GeoEntityRenderer<VampireEntity> {

  static final float SHADOW_RADIUS = 0.5F;

  private final BatFormRenderers batForm;
  private final MistFormRenderer mist;

  public VampireRenderer(final EntityRendererFactory.Context context) {
    super(context, new VampireModel());
    this.batForm = new BatFormRenderers(context);
    this.mist = new MistFormRenderer(context);
    this.shadowRadius = SHADOW_RADIUS;
    addRenderLayer(new VampireStageMarksLayer(this));
    addRenderLayer(new VampireEyesLayer(this));
    addRenderLayer(new RigArmorLayer<>(this, this::getAnimatable));
    addRenderLayer(new HandItemGeoLayer<>(this));
  }

  @Override
  public void render(
      final VampireEntity vampire,
      final float yaw,
      final float tickDelta,
      final MatrixStack matrices,
      final VertexConsumerProvider vertexConsumers,
      final int light) {
    final boolean hidden = UnseenBodies.bodyHidden(vampire);
    this.shadowRadius = hidden ? 0.0F : SHADOW_RADIUS;
    if (MistForm.on(vampire) && !hidden) {
      mist.render(vampire, yaw, tickDelta, matrices, vertexConsumers, light);
      return;
    }
    if (BatForm.isBat(vampire) && !hidden) {
      batForm.render(vampire, yaw, tickDelta, matrices, vertexConsumers, light);
      return;
    }
    super.render(vampire, yaw, tickDelta, matrices, vertexConsumers, light);
  }

  @Override
  protected float getDeathMaxRotation(final VampireEntity vampire) {
    return StakedFall.deathRoll(StakedFall.staked(vampire), super.getDeathMaxRotation(vampire));
  }

  @Override
  public boolean hasLabel(final VampireEntity vampire) {
    return !UnseenBodies.bodyHidden(vampire) && super.hasLabel(vampire);
  }

  @Override
  public RenderLayer getRenderType(
      final VampireEntity vampire,
      final Identifier texture,
      final VertexConsumerProvider bufferSource,
      final float partialTick) {
    if (UnseenBodies.bodyHidden(vampire)) {
      return RenderLayer.getOutline(texture);
    }
    return super.getRenderType(vampire, texture, bufferSource, partialTick);
  }

  @Override
  public void applyRenderLayers(
      final MatrixStack poseStack,
      final VampireEntity vampire,
      final BakedGeoModel model,
      final RenderLayer renderType,
      final VertexConsumerProvider bufferSource,
      final VertexConsumer buffer,
      final float partialTick,
      final int packedLight,
      final int packedOverlay) {
    if (!UnseenBodies.bodyHidden(vampire)) {
      super.applyRenderLayers(
          poseStack,
          vampire,
          model,
          renderType,
          bufferSource,
          buffer,
          partialTick,
          packedLight,
          packedOverlay);
    }
  }

  @Override
  public void applyRenderLayersForBone(
      final MatrixStack poseStack,
      final VampireEntity vampire,
      final GeoBone bone,
      final RenderLayer renderType,
      final VertexConsumerProvider bufferSource,
      final VertexConsumer buffer,
      final float partialTick,
      final int packedLight,
      final int packedOverlay) {
    if (!UnseenBodies.bodyHidden(vampire)) {
      super.applyRenderLayersForBone(
          poseStack,
          vampire,
          bone,
          renderType,
          bufferSource,
          buffer,
          partialTick,
          packedLight,
          packedOverlay);
    }
  }

  @Override
  public void renderRecursively(
      final MatrixStack poseStack,
      final VampireEntity animatable,
      final GeoBone bone,
      final RenderLayer renderType,
      final VertexConsumerProvider bufferSource,
      final VertexConsumer buffer,
      final boolean isReRender,
      final float partialTick,
      final int packedLight,
      final int packedOverlay,
      final int colour) {
    poseStack.push();
    BustShape.size(poseStack, bone, animatable.bust());
    super.renderRecursively(
        poseStack,
        animatable,
        bone,
        renderType,
        bufferSource,
        buffer,
        isReRender,
        partialTick,
        packedLight,
        packedOverlay,
        colour);
    poseStack.pop();
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
