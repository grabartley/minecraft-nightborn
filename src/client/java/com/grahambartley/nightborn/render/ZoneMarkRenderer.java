package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.boss.ZoneMarkEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ZoneMarkRenderer extends GeoEntityRenderer<ZoneMarkEntity> {

  static final int FULL_BRIGHT = 0xF000F0;
  static final float MODEL_HALF_WIDTH = 1.0F;
  static final float THIN = 0.5F;

  public ZoneMarkRenderer(final EntityRendererFactory.Context context) {
    super(context, new ZoneMarkModel());
    this.shadowRadius = 0.0F;
  }

  static float across(final float radius) {
    return radius / MODEL_HALF_WIDTH;
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
  public RenderLayer getRenderType(
      final ZoneMarkEntity mark,
      final Identifier texture,
      @Nullable final VertexConsumerProvider vertices,
      final float partialTick) {
    return RenderLayer.getEntityTranslucent(texture);
  }
}
