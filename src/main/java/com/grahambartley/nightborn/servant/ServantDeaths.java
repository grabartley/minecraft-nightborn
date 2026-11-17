package com.grahambartley.nightborn.servant;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;

public final class ServantDeaths {

  private ServantDeaths() {}

  public static void tell(final LivingEntity servant, final Optional<UUID> owner) {
    if (!(servant.getWorld() instanceof ServerWorld world)) {
      return;
    }
    final Optional<ServerPlayerEntity> player =
        owner.map(world.getServer().getPlayerManager()::getPlayer);
    if (tells(world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES), player.isPresent())) {
      player.get().sendMessage(servant.getDamageTracker().getDeathMessage());
    }
  }

  static boolean tells(final boolean showDeathMessages, final boolean ownerOnline) {
    return showDeathMessages && ownerOnline;
  }
}
