package com.grahambartley.nightborn.network;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public final class PlayerCues {

  private PlayerCues() {}

  public static void send(final ServerPlayerEntity player, final PlayerCue cue) {
    final PlayerCuePayload payload = new PlayerCuePayload(player.getId(), cue);
    ServerPlayNetworking.send(player, payload);
    PlayerLookup.tracking(player).forEach(watcher -> ServerPlayNetworking.send(watcher, payload));
  }
}
