package com.grahambartley.nightborn.client.name;

import com.grahambartley.nightborn.accessory.GiftAttributes;
import com.grahambartley.nightborn.name.NameBearer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public final class NameSight {

  private NameSight() {}

  public static boolean shows(final Entity entity, final boolean shown) {
    final ClientPlayerEntity player = MinecraftClient.getInstance().player;
    return shows(
            shown,
            entity instanceof NameBearer,
            player != null && !entity.isInvisibleTo(player),
            player != null && player.canSee(entity))
        && withinRange(
            MinecraftClient.getInstance()
                .getEntityRenderDispatcher()
                .getSquaredDistanceToCamera(entity),
            entity instanceof LivingEntity living
                ? GiftAttributes.value(living, GiftAttributes.NAME_RANGE)
                : 0.0);
  }

  static boolean withinRange(final double squaredDistance, final double range) {
    return range <= 0.0 || squaredDistance <= range * range;
  }

  static boolean shows(
      final boolean shown, final boolean bearer, final boolean visible, final boolean inSight) {
    return shown && (!bearer || (visible && inSight));
  }
}
