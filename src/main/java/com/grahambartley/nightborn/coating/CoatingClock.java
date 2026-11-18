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
      if (coat == null || Coatings.isAmmo(stack)) {
        continue;
      }
      coat.afterCheck(CoatingTerms.of(coat.coating(), ServerConfigService.current(), stack), now)
          .ifPresentOrElse(
              left -> {
                if (!left.equals(coat)) {
                  stack.set(Coatings.COAT, left);
                }
              },
              () -> {
                stack.remove(Coatings.COAT);
                coat.coating().wearOff(player.getServerWorld(), player);
              });
    }
  }
}
