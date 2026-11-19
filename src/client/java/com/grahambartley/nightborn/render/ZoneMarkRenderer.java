package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.boss.ZoneMarkEntity;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ZoneMarkRenderer extends GeoEntityRenderer<ZoneMarkEntity> {

  static final int FULL_BRIGHT = 0xF000F0;
  static final float MODEL_HALF_WIDTH = 1.0F;
  static final float THIN = 0.5F;
  static final String PILLAR = "pillar";
  static final float PILLAR_HEIGHT = 3.0F;
  static final float MIN_REACH_HEIGHT = 1.0F;

  public ZoneMarkRenderer(final EntityRendererFactory.Context context) {
    super(context, new ZoneMarkModel());
    this.shadowRadius = 0.0F;
  }

  static float across(final float radius) {
    return radius / MODEL_HALF_WIDTH;
  }

  static float rise(final float height) {
    return height / (PILLAR_HEIGHT * THIN);
  }

  static Box reach(
      final double x,
      final double y,
      final double z,
      final float radius,
      final float length,
      final float rise) {
    final double half = Math.max(radius, length / 2.0F);
    return new Box(x - half, y, z - half, x + half, y + Math.max(rise, MIN_REACH_HEIGHT), z + half);
  }

  @Override
  public boolean shouldRender(
      final ZoneMarkEntity mark,
      final Frustum frustum,
      final double cameraX,
      final double cameraY,
      final double cameraZ) {
    return frustum.isVisible(
        reach(mark.getX(), mark.getY(), mark.getZ(), mark.radius(), mark.length(), mark.rise()));
  }

  static float along(final boolean line, final float radius, final float length) {
    return (line ? length / 2.0F : radius) / MODEL_HALF_WIDTH;
  }

  @Override
  public void render(
      final ZoneMarkEntity mark,
      final float entityYaw,
      final float partialTick,
      final MatrixStack matrices,
      final VertexConsumerProvider vertices,
      final int packedLight) {
    matrices.push();
    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-mark.getYaw()));
    matrices.scale(across(mark.radius()), THIN, along(mark.line(), mark.radius(), mark.length()));
    super.render(mark, entityYaw, partialTick, matrices, vertices, FULL_BRIGHT);
    matrices.pop();
  }

  @Override
  public void renderRecursively(
      final MatrixStack poseStack,
      final ZoneMarkEntity mark,
      final GeoBone bone,
      final RenderLayer renderType,
      final VertexConsumerProvider bufferSource,
      final VertexConsumer buffer,
      final boolean isReRender,
      final float partialTick,
      final int packedLight,
      final int packedOverlay,
      final int colour) {
    final boolean pillar = PILLAR.equals(bone.getName());
    if (pillar && mark.rise() <= 0.0F) {
      return;
    }
    poseStack.push();
    if (pillar) {
      poseStack.scale(1.0F, rise(mark.rise()), 1.0F);
    }
    super.renderRecursively(
        poseStack,
        mark,
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
  public RenderLayer getRenderType(
      final ZoneMarkEntity mark,
      final Identifier texture,
      @Nullable final VertexConsumerProvider vertices,
      final float partialTick) {
    return RenderLayer.getEntityTranslucent(texture);
  }
}
