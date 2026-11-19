package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.occult.HexMark;
import com.grahambartley.nightborn.occult.Hexes;
import com.grahambartley.nightborn.seeker.Quarries;
import com.grahambartley.nightborn.trap.Grounding;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public final class StatusMarks {

  private static StatusMarkRenderer net;
  private static StatusMarkRenderer hex;
  private static StatusMarkRenderer quarry;

  private StatusMarks() {}

  public static void draw(
      final Entity entity,
      final MatrixStack matrices,
      final VertexConsumerProvider provider,
      final int light,
      final float tickDelta) {
    if (!(entity instanceof LivingEntity living)) {
      return;
    }
    final long now = living.getWorld().getTime();
    final boolean invisible = living.isInvisible();
    final float width = living.getWidth();
    final float height = living.getHeight();
    if (StatusMarkView.shows(invisible, living.getAttached(Grounding.TYPE), now)) {
      renderers();
      net.draw(
          matrices,
          StatusMarkAnimatable.NET,
          provider,
          StatusMarkView.netWidth(width),
          StatusMarkView.netHeight(height),
          light,
          tickDelta);
    }
    final HexMark hexMark = living.getAttached(Hexes.TYPE);
    if (StatusMarkView.shows(invisible, hexMark == null ? null : hexMark.until(), now)) {
      renderers();
      final float scale = StatusMarkView.hexScale(width);
      matrices.push();
      matrices.translate(0.0F, StatusMarkView.hexLift(height), 0.0F);
      hex.draw(matrices, StatusMarkAnimatable.HEX, provider, scale, scale, light, tickDelta);
      matrices.pop();
    }
    if (StatusMarkView.shows(invisible, living.getAttached(Quarries.QUARRIED), now)) {
      renderers();
      final float scale = StatusMarkView.quarryScale(width);
      quarry.draw(matrices, StatusMarkAnimatable.QUARRY, provider, scale, scale, light, tickDelta);
    }
  }

  private static void renderers() {
    if (net == null) {
      net = new StatusMarkRenderer(StatusMarkAnimatable.NET, false);
      hex = new StatusMarkRenderer(StatusMarkAnimatable.HEX, true);
      quarry = new StatusMarkRenderer(StatusMarkAnimatable.QUARRY, true);
    }
  }
}
