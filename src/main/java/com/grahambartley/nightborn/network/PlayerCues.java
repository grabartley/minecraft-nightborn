package com.grahambartley.nightborn.network;

import net.minecraft.server.network.ServerPlayerEntity;

public final class PlayerCues {

  private PlayerCues() {}

  public static void send(final ServerPlayerEntity player, final PlayerCue cue) {
    TrackingSends.toSelfAndWatchers(player, new PlayerCuePayload(player.getId(), cue));
  }

  public static void power(final ServerPlayerEntity player, final String clip) {
    TrackingSends.toSelfAndWatchers(player, new PowerClipPayload(player.getId(), clip));
  }

  public static void stopPower(final ServerPlayerEntity player) {
    power(player, "");
  }
}
