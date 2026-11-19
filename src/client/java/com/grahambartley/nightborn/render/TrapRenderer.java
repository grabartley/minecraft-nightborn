package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.boss.ZoneMarkEntity;
import com.grahambartley.nightborn.trap.TrapEntity;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.player.PlayerEntity;

public class TrapRenderer extends ZoneMarkRenderer {

  public TrapRenderer(final EntityRendererFactory.Context context) {
    super(context);
  }

  static boolean shown(
      final boolean viewerVampire, final double squaredDistance, final float seenWithin) {
    return !viewerVampire || squaredDistance <= (double) seenWithin * seenWithin;
  }

  @Override
  public boolean shouldRender(
      final ZoneMarkEntity mark,
      final Frustum frustum,
      final double x,
      final double y,
      final double z) {
    final PlayerEntity viewer = MinecraftClient.getInstance().player;
    if (mark instanceof TrapEntity trap
        && viewer != null
        && !shown(Vampires.isVampire(viewer), viewer.squaredDistanceTo(trap), trap.seenWithin())) {
      return false;
    }
    return super.shouldRender(mark, frustum, x, y, z);
  }
}
