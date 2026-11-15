package com.grahambartley.nightborn.coating;

import com.grahambartley.nightborn.server.ServerConfigService;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class CoatingClock {

  static final int CHECK_TICKS = 20;

  private CoatingClock() {}

  static void tick(final MinecraftServer server) {
    if (server.getTicks() % CHECK_TICKS != 0) {
      return;
    }
    for (final ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
      check(player);
    }
  }

  static void check(final ServerPlayerEntity player) {
    final long now = player.getServerWorld().getTime();
    final PlayerInventory inventory = player.getInventory();
    for (int slot = 0; slot < inventory.size(); slot++) {
      final ItemStack stack = inventory.getStack(slot);
      final Coat coat = stack.get(Coatings.COAT);
      if (coat == null || stack.isIn(Coatings.AMMO)) {
        continue;
      }
      if (!coat.started()) {
        stack.set(Coatings.COAT, coat.startingAt(now));
      } else if (coat.worn(CoatingTerms.of(coat.coating(), ServerConfigService.current()), now)) {
        stack.remove(Coatings.COAT);
        CoatedStrikes.wearOff(player.getServerWorld(), player, coat.coating());
      }
    }
  }
}
