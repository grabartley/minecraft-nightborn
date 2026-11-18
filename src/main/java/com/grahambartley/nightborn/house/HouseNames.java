package com.grahambartley.nightborn.house;

import com.grahambartley.nightborn.name.NameBearer;
import com.grahambartley.nightborn.name.StageNames;
import com.grahambartley.nightborn.network.HouseNamePayload;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class HouseNames {

  static final int REVIEW_TICKS = 20;

  private static final Map<UUID, String> SHOWN = new HashMap<>();

  private HouseNames() {}

  static void register() {
    EntityTrackingEvents.START_TRACKING.register(
        (tracked, viewer) -> {
          if (tracked instanceof NameBearer && !(tracked instanceof ServerPlayerEntity)) {
            ServerPlayNetworking.send(
                viewer,
                new HouseNamePayload(tracked.getId(), of(viewer.getServer(), tracked.getUuid())));
          }
        });
    ServerTickEvents.END_SERVER_TICK.register(
        server -> {
          if (server.getTicks() % REVIEW_TICKS == 0) {
            review(server);
          }
        });
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> SHOWN.clear());
  }

  public static Optional<String> of(final MinecraftServer server, final UUID member) {
    return Houses.get(server).of(member).flatMap(house -> house.identity().name());
  }

  public static Map<UUID, String> names(final Iterable<House> houses) {
    final Map<UUID, String> named = new HashMap<>();
    for (final House house : houses) {
      house
          .identity()
          .name()
          .ifPresent(name -> house.everyone().forEach(member -> named.put(member, name)));
    }
    return named;
  }

  public static Set<UUID> changed(final Map<UUID, String> before, final Map<UUID, String> after) {
    final Set<UUID> changed = new HashSet<>(before.keySet());
    changed.addAll(after.keySet());
    changed.removeIf(
        member ->
            Optional.ofNullable(before.get(member)).equals(Optional.ofNullable(after.get(member))));
    return changed;
  }

  private static void review(final MinecraftServer server) {
    final Map<UUID, String> now = names(Houses.get(server).all());
    final Set<UUID> changed = changed(SHOWN, now);
    SHOWN.clear();
    SHOWN.putAll(now);
    for (final UUID member : changed) {
      announce(server, member, Optional.ofNullable(now.get(member)));
    }
  }

  private static void announce(
      final MinecraftServer server, final UUID member, final Optional<String> house) {
    final ServerPlayerEntity player = server.getPlayerManager().getPlayer(member);
    if (player != null) {
      StageNames.changed(player);
      return;
    }
    HouseRanks.find(server, member)
        .filter(NameBearer.class::isInstance)
        .ifPresent(
            bearer -> {
              final HouseNamePayload payload = new HouseNamePayload(bearer.getId(), house);
              for (final ServerPlayerEntity viewer : PlayerLookup.tracking((Entity) bearer)) {
                ServerPlayNetworking.send(viewer, payload);
              }
            });
  }
}
