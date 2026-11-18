package com.grahambartley.nightborn.structure;

import com.grahambartley.nightborn.network.StructureZonePayload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;

public final class StructureZoneTracker {

  private static final int CHECK_INTERVAL = 20;
  private static final int CHUNK_RADIUS = 2;
  private static final Map<UUID, Optional<StructureZone>> SENT = new HashMap<>();

  private StructureZoneTracker() {}

  public static void register() {
    ServerTickEvents.END_SERVER_TICK.register(StructureZoneTracker::tick);
    ServerPlayConnectionEvents.DISCONNECT.register(
        (handler, server) -> SENT.remove(handler.getPlayer().getUuid()));
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> SENT.clear());
  }

  private static void tick(final MinecraftServer server) {
    if (server.getTicks() % CHECK_INTERVAL != 0) {
      return;
    }
    for (final ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
      final Optional<StructureZone> zone = zoneAround(player);
      StructureVisits.visit(player, zone);
      if (!zone.equals(SENT.getOrDefault(player.getUuid(), Optional.empty()))) {
        SENT.put(player.getUuid(), zone);
        ServerPlayNetworking.send(player, new StructureZonePayload(zone));
      }
    }
  }

  public static Optional<StructureZone> zoneAround(final ServerPlayerEntity player) {
    final ServerWorld world = player.getServerWorld();
    final ChunkPos centre = player.getChunkPos();
    final List<StructureZone> zones = new ArrayList<>();
    for (int dx = -CHUNK_RADIUS; dx <= CHUNK_RADIUS; dx++) {
      for (int dz = -CHUNK_RADIUS; dz <= CHUNK_RADIUS; dz++) {
        Optional.ofNullable(world.getChunkManager().getWorldChunk(centre.x + dx, centre.z + dz))
            .ifPresent(chunk -> collect(world, chunk, zones));
      }
    }
    return StructureZones.deepest(zones, player.getX(), player.getEyeY(), player.getZ());
  }

  private static void collect(
      final ServerWorld world, final Chunk chunk, final List<StructureZone> zones) {
    for (final StructureStart start : VampireStructureStarts.referencedBy(world, chunk)) {
      zoneOf(world, start).ifPresent(zones::add);
    }
  }

  private static Optional<StructureZone> zoneOf(
      final ServerWorld world, final StructureStart start) {
    final Identifier structure =
        world.getRegistryManager().get(RegistryKeys.STRUCTURE).getId(start.getStructure());
    final StructureAmbience ambience = ((VampireStructure) start.getStructure()).ambience();
    return StructureHeart.of(start.getChildren())
        .filter(heart -> structure != null)
        .map(
            heart ->
                new StructureZone(
                    structure, FogZone.around(heart, start.getBoundingBox()), ambience));
  }
}
