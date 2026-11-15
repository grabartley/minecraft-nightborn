package com.grahambartley.nightborn.network;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

public final class TrackingSends {

  private TrackingSends() {}

  public static void toSelfAndWatchers(final Entity entity, final CustomPayload payload) {
    if (entity instanceof ServerPlayerEntity player) {
      ServerPlayNetworking.send(player, payload);
    }
    PlayerLookup.tracking(entity).forEach(watcher -> ServerPlayNetworking.send(watcher, payload));
  }
}
