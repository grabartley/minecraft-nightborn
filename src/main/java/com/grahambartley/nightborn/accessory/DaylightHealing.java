package com.grahambartley.nightborn.accessory;

import com.grahambartley.nightborn.sun.DirectSun;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class DaylightHealing {

  private DaylightHealing() {}

  public static void tick(final MinecraftServer server) {
    for (final ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
      final double heal = GiftAttributes.value(player, GiftAttributes.DAYLIGHT_HEAL);
      if (heal > 0.0
          && heals(
              player.age,
              (int) GiftAttributes.value(player, GiftAttributes.DAYLIGHT_HEAL_INTERVAL),
              player.isAlive() && player.getHealth() < player.getMaxHealth())
          && DirectSun.on(player)) {
        player.heal((float) heal);
      }
    }
  }

  public static boolean heals(final int age, final int interval, final boolean hurt) {
    return hurt && interval > 0 && age > 0 && age % interval == 0;
  }
}
