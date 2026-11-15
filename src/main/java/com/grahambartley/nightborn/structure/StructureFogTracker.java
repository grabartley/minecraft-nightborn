package com.grahambartley.nightborn.structure;

import com.grahambartley.nightborn.network.StructureFogPayload;
import it.unimi.dsi.fastutil.longs.LongSet;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.structure.Structure;

public final class StructureFogTracker {

  private static final int CHECK_INTERVAL = 20;
  private static final int CHUNK_RADIUS = 2;
  private static final Map<UUID, Optional<FogZone>> SENT = new HashMap<>();

  private StructureFogTracker() {}

  public static void register() {
    ServerTickEvents.END_SERVER_TICK.register(StructureFogTracker::tick);
    ServerPlayConnectionEvents.DISCONNECT.register(
        (handler, server) -> SENT.remove(handler.getPlayer().getUuid()));
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> SENT.clear());
  }

  private static void tick(final MinecraftServer server) {
    if (server.getTicks() % CHECK_INTERVAL != 0) {
      return;
    }
    for (final ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
      final Optional<FogZone> zone = zoneAround(player);
      if (!zone.equals(SENT.getOrDefault(player.getUuid(), Optional.empty()))) {
        SENT.put(player.getUuid(), zone);
        ServerPlayNetworking.send(player, new StructureFogPayload(zone));
      }
    }
  }

  public static Optional<FogZone> zoneAround(final ServerPlayerEntity player) {
    final ServerWorld world = player.getServerWorld();
    final ChunkPos centre = player.getChunkPos();
    final List<FogZone> zones = new ArrayList<>();
    for (int dx = -CHUNK_RADIUS; dx <= CHUNK_RADIUS; dx++) {
      for (int dz = -CHUNK_RADIUS; dz <= CHUNK_RADIUS; dz++) {
        loaded(world, centre.x + dx, centre.z + dz)
            .ifPresent(chunk -> collect(world, chunk, zones));
      }
    }
    return FogZones.deepest(zones, player.getX(), player.getEyeY(), player.getZ());
  }

  private static void collect(
      final ServerWorld world, final Chunk chunk, final List<FogZone> zones) {
    for (final Map.Entry<Structure, LongSet> entry : chunk.getStructureReferences().entrySet()) {
      if (entry.getKey().getType() != NightbornStructures.VAMPIRE_STRUCTURE) {
        continue;
      }
      for (final long reference : entry.getValue()) {
        final ChunkPos home = new ChunkPos(reference);
        loaded(world, home.x, home.z)
            .map(startChunk -> startChunk.getStructureStart(entry.getKey()))
            .ifPresent(start -> zoneOf(start).ifPresent(zones::add));
      }
    }
  }

  private static Optional<FogZone> zoneOf(final StructureStart start) {
    return StructureHeart.of(start.getChildren())
        .map(heart -> FogZone.around(heart, start.getBoundingBox()));
  }

  private static Optional<Chunk> loaded(final ServerWorld world, final int x, final int z) {
    return Optional.ofNullable(world.getChunkManager().getWorldChunk(x, z));
  }
}
