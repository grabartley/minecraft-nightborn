package com.grahambartley.nightborn.structure;

import com.grahambartley.nightborn.Nightborn;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.structure.Structure;

public final class NearestStructures {

  public static final long DAY_TICKS = 24000L;

  private static final NearestStructureCache CACHE = new NearestStructureCache();
  private static final Executor SEARCHER =
      Executors.newSingleThreadExecutor(
          Thread.ofPlatform().name("Nightborn structure search").daemon().factory());

  private NearestStructures() {}

  public static void register() {
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> CACHE.forget());
  }

  public record Found(Identifier structure, BlockPos pos) {}

  public static CompletableFuture<List<Found>> eachOf(
      final ServerWorld world, final TagKey<Structure> tag, final BlockPos from, final int range) {
    final NearestStructureCache.Ask ask =
        new NearestStructureCache.Ask(
            world.getRegistryKey().getValue(),
            tag.id(),
            new ChunkPos(from).toLong(),
            range,
            Math.floorDiv(world.getTimeOfDay(), DAY_TICKS));
    final List<RegistryEntry<Structure>> entries =
        world
            .getRegistryManager()
            .get(RegistryKeys.STRUCTURE)
            .getEntryList(tag)
            .<List<RegistryEntry<Structure>>>map(list -> list.stream().toList())
            .orElse(List.of());
    final ChunkGenerator generator = world.getChunkManager().getChunkGenerator();
    final StructurePlacementCalculator placements =
        world.getChunkManager().getStructurePlacementCalculator();
    return CACHE
        .find(
            ask,
            () ->
                CompletableFuture.supplyAsync(
                    () -> locate(world, generator, placements, entries, from, range), SEARCHER))
        .exceptionally(
            error -> {
              Nightborn.LOGGER.warn("Structure search for {} failed", tag.id(), error);
              return List.of();
            });
  }

  private static List<Found> locate(
      final ServerWorld world,
      final ChunkGenerator generator,
      final StructurePlacementCalculator placements,
      final List<RegistryEntry<Structure>> entries,
      final BlockPos from,
      final int range) {
    final List<Found> found = new ArrayList<>();
    for (final RegistryEntry<Structure> entry : entries) {
      final Pair<BlockPos, RegistryEntry<Structure>> hit =
          generator.locateStructure(
              world,
              RegistryEntryList.of(entry),
              from,
              LocateRadius.of(range, placements.getPlacements(entry)),
              false);
      if (hit != null) {
        entry.getKey().ifPresent(key -> found.add(new Found(key.getValue(), hit.getFirst())));
      }
    }
    return found;
  }
}
