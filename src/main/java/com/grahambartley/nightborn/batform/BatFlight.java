package com.grahambartley.nightborn.batform;

import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.server.network.ServerPlayerEntity;

final class BatFlight {

  private BatFlight() {}

  static void lift(final ServerPlayerEntity player) {
    final PlayerAbilities abilities = player.getAbilities();
    abilities.allowFlying = true;
    abilities.flying = true;
    player.sendAbilitiesUpdate();
  }

  static void keep(final ServerPlayerEntity player) {
    if (!player.getAbilities().allowFlying) {
      player.getAbilities().allowFlying = true;
      player.sendAbilitiesUpdate();
    }
  }

  static void ground(final ServerPlayerEntity player) {
    final PlayerAbilities abilities = player.getAbilities();
    player.interactionManager.getGameMode().setAbilities(abilities);
    abilities.flying = abilities.flying && abilities.allowFlying;
    player.sendAbilitiesUpdate();
  }
}
