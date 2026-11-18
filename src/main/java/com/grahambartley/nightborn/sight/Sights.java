package com.grahambartley.nightborn.sight;

import com.grahambartley.nightborn.blood.BloodQuality;
import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.network.RevealPayload;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

public final class Sights {

  static final int SEND_EVERY_TICKS = 10;
  static final int SHOW_TICKS = 15;

  private static final Map<UUID, List<Sight>> SIGHTS = new HashMap<>();

  private Sights() {}

  public static void register() {
    ServerTickEvents.END_SERVER_TICK.register(Sights::tick);
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> SIGHTS.clear());
  }

  public static void give(final LivingEntity viewer, final Sight sight) {
    if (!(viewer instanceof ServerPlayerEntity player)) {
      return;
    }
    SIGHTS.computeIfAbsent(player.getUuid(), id -> new ArrayList<>()).add(sight);
    send(player, SIGHTS.get(player.getUuid()));
  }

  private static void tick(final MinecraftServer server) {
    if (SIGHTS.isEmpty() || server.getTicks() % SEND_EVERY_TICKS != 0) {
      return;
    }
    SIGHTS
        .entrySet()
        .removeIf(
            entry -> {
              final ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
              if (player == null || !player.isAlive()) {
                return true;
              }
              final long now = player.getServerWorld().getTime();
              entry.getValue().removeIf(sight -> sight.over(now));
              if (entry.getValue().isEmpty()) {
                return true;
              }
              send(player, entry.getValue());
              return false;
            });
  }

  private static void send(final ServerPlayerEntity player, final List<Sight> sights) {
    final Map<Integer, Integer> shown = new LinkedHashMap<>();
    for (final Sight sight : sights) {
      sight
          .marked()
          .map(player.getServerWorld()::getEntity)
          .filter(Entity::isAlive)
          .ifPresent(marked -> shown.put(marked.getId(), sight.colour()));
      if (sight.senseRange() > 0.0F) {
        heartbeats(player, sight.senseRange(), shown);
      }
    }
    ServerPlayNetworking.send(
        player,
        new RevealPayload(List.copyOf(shown.keySet()), List.copyOf(shown.values()), SHOW_TICKS));
  }

  private static void heartbeats(
      final ServerPlayerEntity player, final float range, final Map<Integer, Integer> shown) {
    final NightbornConfig config = ServerConfigService.current();
    final int most =
        Math.max(
            BloodQuality.PLAYER.worthPercent(config), BloodQuality.VILLAGER.worthPercent(config));
    for (final LivingEntity living :
        player
            .getServerWorld()
            .getEntitiesByClass(
                LivingEntity.class,
                new Box(player.getBlockPos()).expand(range),
                candidate ->
                    candidate != player
                        && candidate.isAlive()
                        && !candidate.isSpectator()
                        && !Vampires.isVampire(candidate)
                        && candidate.squaredDistanceTo(player) <= (double) range * range)) {
      BloodQuality.of(living.getType())
          .ifPresent(
              quality ->
                  shown.putIfAbsent(
                      living.getId(),
                      Heartbeat.colour(
                          Heartbeat.weight(
                              quality.worthPercent(config),
                              most,
                              living.getHealth(),
                              living.getMaxHealth()))));
    }
  }
}
