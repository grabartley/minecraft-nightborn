package com.grahambartley.nightborn.client.boss;

import com.grahambartley.nightborn.boss.Arena;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public final class ArenaWallRenderer {

  static final Identifier TEXTURE = Identifier.ofVanilla("textures/misc/forcefield.png");
  static final int FULL_BRIGHT = 0xF000F0;
  static final int RED = 0x7A;
  static final int GREEN = 0x0E;
  static final int BLUE = 0x18;
  static final float DRIFT = 0.004F;
  static final float TILE = 2.0F;

  private ArenaWallRenderer() {}

  public static void render(final WorldRenderContext context) {
    final VertexConsumerProvider consumers = context.consumers();
    final MatrixStack matrices = context.matrixStack();
    if (BossArenaView.arena().isEmpty() || consumers == null || matrices == null) {
      return;
    }
    final Arena arena = BossArenaView.arena().get();
    final Vec3d camera = context.camera().getPos();
    final double fromWall =
        Math.hypot(camera.x - arena.midX(), camera.z - arena.midZ()) - arena.radius();
    final int colour =
        ColorHelper.Argb.getArgb((int) (ArenaWallShape.alpha(fromWall) * 255.0F), RED, GREEN, BLUE);
    final float drift =
        (context.world().getTime() + context.tickCounter().getTickDelta(false)) * DRIFT;
    final VertexConsumer buffer = consumers.getBuffer(RenderLayer.getEntityTranslucent(TEXTURE));
    final float low = arena.centre().getY() - arena.below();
    final float high = arena.centre().getY() + arena.above();
    final int segments = ArenaWallShape.segments(arena.radius());
    matrices.push();
    matrices.translate(arena.midX() - camera.x, -camera.y, arena.midZ() - camera.z);
    final Matrix4f matrix = matrices.peek().getPositionMatrix();
    for (int i = 0; i < segments; i++) {
      final float x0 = (float) ArenaWallShape.x(i, segments, arena.radius());
      final float z0 = (float) ArenaWallShape.z(i, segments, arena.radius());
      final float x1 = (float) ArenaWallShape.x(i + 1, segments, arena.radius());
      final float z1 = (float) ArenaWallShape.z(i + 1, segments, arena.radius());
      final float u0 = i / TILE;
      final float u1 = (i + 1) / TILE;
      final float v0 = drift;
      final float v1 = drift + (high - low) / TILE;
      corner(matrices, buffer, matrix, x0, low, z0, u0, v1, colour);
      corner(matrices, buffer, matrix, x1, low, z1, u1, v1, colour);
      corner(matrices, buffer, matrix, x1, high, z1, u1, v0, colour);
      corner(matrices, buffer, matrix, x0, high, z0, u0, v0, colour);
    }
    matrices.pop();
  }

  private static void corner(
      final MatrixStack matrices,
      final VertexConsumer buffer,
      final Matrix4f matrix,
      final float x,
      final float y,
      final float z,
      final float u,
      final float v,
      final int colour) {
    buffer
        .vertex(matrix, x, y, z)
        .color(colour)
        .texture(u, v)
        .overlay(OverlayTexture.DEFAULT_UV)
        .light(FULL_BRIGHT)
        .normal(matrices.peek(), 0.0F, 1.0F, 0.0F);
  }
}
