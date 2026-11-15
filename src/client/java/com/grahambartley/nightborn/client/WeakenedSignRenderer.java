package com.grahambartley.nightborn.client;

import com.grahambartley.nightborn.vampire.VisibleStateAttachment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public final class WeakenedSignRenderer {

  static final float SIZE = 0.4F;
  static final double ABOVE_HEAD = 0.55;
  static final int FULL_BRIGHT = 0xF000F0;

  private WeakenedSignRenderer() {}

  public static void render(final WorldRenderContext context) {
    final ClientWorld world = context.world();
    final VertexConsumerProvider consumers = context.consumers();
    final MatrixStack matrices = context.matrixStack();
    if (world == null || consumers == null || matrices == null) {
      return;
    }
    final MinecraftClient client = MinecraftClient.getInstance();
    final Sprite sprite = client.getStatusEffectSpriteManager().getSprite(StatusEffects.WEAKNESS);
    final VertexConsumer buffer =
        consumers.getBuffer(RenderLayer.getEntityTranslucent(sprite.getAtlasId()));
    final Camera camera = context.camera();
    final float tickDelta = context.tickCounter().getTickDelta(false);
    for (final AbstractClientPlayerEntity player : world.getPlayers()) {
      final boolean ownFirstPerson = player == camera.getFocusedEntity() && !camera.isThirdPerson();
      if (ownFirstPerson
          || player.isInvisible()
          || !VisibleStateAttachment.get(player).isWeakened()) {
        continue;
      }
      final Vec3d at =
          player
              .getLerpedPos(tickDelta)
              .add(0.0, player.getHeight() + ABOVE_HEAD, 0.0)
              .subtract(camera.getPos());
      matrices.push();
      matrices.translate(at.x, at.y, at.z);
      matrices.multiply(camera.getRotation());
      quad(matrices, buffer, sprite);
      matrices.pop();
    }
  }

  private static void quad(
      final MatrixStack matrices, final VertexConsumer buffer, final Sprite sprite) {
    final Matrix4f matrix = matrices.peek().getPositionMatrix();
    final float half = SIZE / 2.0F;
    corner(matrices, buffer, matrix, -half, -half, sprite.getMinU(), sprite.getMaxV());
    corner(matrices, buffer, matrix, half, -half, sprite.getMaxU(), sprite.getMaxV());
    corner(matrices, buffer, matrix, half, half, sprite.getMaxU(), sprite.getMinV());
    corner(matrices, buffer, matrix, -half, half, sprite.getMinU(), sprite.getMinV());
  }

  private static void corner(
      final MatrixStack matrices,
      final VertexConsumer buffer,
      final Matrix4f matrix,
      final float x,
      final float y,
      final float u,
      final float v) {
    buffer
        .vertex(matrix, x, y, 0.0F)
        .color(0xFFFFFFFF)
        .texture(u, v)
        .overlay(OverlayTexture.DEFAULT_UV)
        .light(FULL_BRIGHT)
        .normal(matrices.peek(), 0.0F, 0.0F, 1.0F);
  }
}
