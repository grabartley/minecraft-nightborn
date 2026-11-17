package com.grahambartley.nightborn.client.boss;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.boss.Arena;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public final class ArenaWallRenderer {

  static final Identifier FOG =
      Identifier.of(Nightborn.MOD_ID, "textures/entity/boss/arena_fog.png");
  static final Identifier SHAPES =
      Identifier.of(Nightborn.MOD_ID, "textures/entity/boss/arena_shapes.png");
  static final int FULL_BRIGHT = 0xF000F0;

  private record Veil(
      Identifier texture,
      boolean surfaces,
      float inset,
      float tile,
      float climb,
      float swirl,
      float alpha,
      double tintLead) {}

  static final Veil[] VEILS = {
    new Veil(FOG, false, 0.0F, 6.0F, 0.006F, 0.002F, 1.0F, 0.0),
    new Veil(FOG, false, 0.12F, 9.0F, -0.011F, -0.005F, 0.7F, Math.PI),
    new Veil(SHAPES, true, 0.2F, 8.0F, 0.003F, 0.0015F, 1.0F, Math.PI / 2.0)
  };

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
    final float time = context.world().getTime() + context.tickCounter().getTickDelta(false);
    final float strength = ArenaWallShape.strength(BossArenaView.fighting());
    final float alpha = ArenaWallShape.alpha(fromWall) * ArenaWallShape.pulse(time * strength);
    matrices.push();
    matrices.translate(arena.midX() - camera.x, -camera.y, arena.midZ() - camera.z);
    for (final Veil veil : VEILS) {
      final VertexConsumer buffer =
          consumers.getBuffer(RenderLayer.getEntityTranslucent(veil.texture()));
      veil(
          matrices,
          buffer,
          arena,
          veil,
          time,
          strength,
          veil.surfaces() ? alpha * ArenaWallShape.surfacing(time * strength) : alpha);
    }
    matrices.pop();
  }

  private static void veil(
      final MatrixStack matrices,
      final VertexConsumer buffer,
      final Arena arena,
      final Veil veil,
      final float time,
      final float strength,
      final float alpha) {
    final float low = arena.centre().getY() - arena.below();
    final float rise = (float) (arena.below() + arena.above()) / ArenaWallShape.ROWS;
    final int segments = ArenaWallShape.segments(arena.radius());
    for (int i = 0; i < segments; i++) {
      for (int row = 0; row < ArenaWallShape.ROWS; row++) {
        final float y0 = low + row * rise;
        final float y1 = y0 + rise;
        corner(matrices, buffer, arena, veil, i, segments, y0, time, strength, alpha);
        corner(matrices, buffer, arena, veil, i + 1, segments, y0, time, strength, alpha);
        corner(matrices, buffer, arena, veil, i + 1, segments, y1, time, strength, alpha);
        corner(matrices, buffer, arena, veil, i, segments, y1, time, strength, alpha);
      }
    }
  }

  private static void corner(
      final MatrixStack matrices,
      final VertexConsumer buffer,
      final Arena arena,
      final Veil veil,
      final int segment,
      final int segments,
      final float y,
      final float time,
      final float strength,
      final float alpha) {
    final double angle = 2.0 * Math.PI * (segment % segments) / segments;
    final double reach =
        arena.radius() - veil.inset() + ArenaWallShape.ripple(angle, y, time, strength);
    final int tint = ArenaWallShape.tint(angle + veil.tintLead(), time * strength);
    buffer
        .vertex(
            matrices.peek().getPositionMatrix(),
            (float) (Math.cos(angle) * reach),
            y,
            (float) (Math.sin(angle) * reach))
        .color(
            tint >> 16 & 0xFF, tint >> 8 & 0xFF, tint & 0xFF, (int) (alpha * veil.alpha() * 255.0F))
        .texture(
            segment / veil.tile() + time * veil.swirl() * strength,
            (0.0F - y) / veil.tile() + time * veil.climb() * strength)
        .overlay(OverlayTexture.DEFAULT_UV)
        .light(FULL_BRIGHT)
        .normal(matrices.peek(), 0.0F, 1.0F, 0.0F);
  }
}
