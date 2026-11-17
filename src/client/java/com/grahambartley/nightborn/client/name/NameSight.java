package com.grahambartley.nightborn.client.name;

import com.grahambartley.nightborn.name.NameBearer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;

public final class NameSight {

  private NameSight() {}

  public static boolean shows(final Entity entity, final boolean shown) {
    final ClientPlayerEntity player = MinecraftClient.getInstance().player;
    return shows(
        shown,
        entity instanceof NameBearer,
        player != null && !entity.isInvisibleTo(player),
        player != null && player.canSee(entity));
  }

  static boolean shows(
      final boolean shown, final boolean bearer, final boolean visible, final boolean inSight) {
    return shown && (!bearer || (visible && inSight));
  }
}
