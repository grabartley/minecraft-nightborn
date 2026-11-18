package com.grahambartley.nightborn.challenge;

import com.grahambartley.nightborn.core.NightTime;
import java.util.stream.StreamSupport;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public final class DeedFacts {

  public static final int FAR = 1_000;

  private DeedFacts() {}

  public static boolean armoured(final PlayerEntity player) {
    return StreamSupport.stream(player.getArmorItems().spliterator(), false)
        .anyMatch(stack -> !stack.isEmpty());
  }

  public static int nearestOtherPlayer(final ServerPlayerEntity player) {
    return nearest(
        player.getServerWorld().getPlayers().stream()
            .filter(other -> other != player && !other.isSpectator())
            .mapToDouble(other -> other.distanceTo(player))
            .min()
            .orElse(FAR));
  }

  public static int nearest(final double distance) {
    return (int) Math.min(FAR, Math.floor(Math.max(0.0, distance)));
  }

  public static boolean byDay(final ServerPlayerEntity player) {
    return !NightTime.isNight(false, player.getServer().getOverworld().getTimeOfDay());
  }
}
