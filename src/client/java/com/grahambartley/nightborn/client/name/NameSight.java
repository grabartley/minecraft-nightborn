package com.grahambartley.nightborn.client.name;

import com.grahambartley.nightborn.name.NameBearer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

public final class NameSight {

  private NameSight() {}

  public static boolean shows(final Entity entity, final boolean shown) {
    if (!(entity instanceof NameBearer)) {
      return shown;
    }
    final MinecraftClient client = MinecraftClient.getInstance();
    return shown
        && client.player != null
        && !entity.isInvisibleTo(client.player)
        && client.player.canSee(entity);
  }
}
