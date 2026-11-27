package com.grahambartley.nightborn.structure;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.scanner.SelectiveNbtCollector;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.structure.JigsawStructure;
import net.minecraft.world.gen.structure.Structure;

public final class NearestStructures {

  public static final long DAY_TICKS = 24000L;
  public static final long SCAN_SECONDS = 5L;

  private static final NearestStructureCache CACHE = new NearestStructureCache();
  private static volatile ExecutorService searcher;

  private NearestStructures() {}

  public static void register() {
    ServerLifecycleEvents.SERVER_STARTING.register(
        server ->
            searcher =
                Executors.newSingleThreadExecutor(
                    Thread.ofPlatform().name("Nightborn structure search").daemon().factory()));
    ServerLifecycleEvents.SERVER_STOPPED.register(
        server -> {
          final ExecutorService stopping = searcher;
          searcher = null;
          if (stopping != null) {
            stopping.shutdownNow();
          }
          CACHE.forget();
        });
    ServerConfigService.onChange((server, config) -> CACHE.forget());
  }

  public record Found(Identifier structure, BlockPos pos) {}

  public static CompletableFuture<List<Found>> eachOf(
      final ServerWorld world, final TagKey<Structure> tag, final BlockPos from, final int range) {
    final ExecutorService running = searcher;
    if (running == null) {
      return CompletableFuture.failedFuture(new IllegalStateException("No server is running"));
    }
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
    return CACHE
        .find(ask, () -> search(running, () -> locate(world, entries, from, range)))
        .whenComplete(
            (found, error) -> {
              if (error != null) {
                Nightborn.LOGGER.warn("Structure search for {} failed", tag.id(), error);
              }
            });
  }

  static CompletableFuture<List<Found>> search(
      final ExecutorService running, final Supplier<List<Found>> walk) {
    try {
      return CompletableFuture.supplyAsync(walk, running);
    } catch (final RejectedExecutionException stopped) {
      return CompletableFuture.failedFuture(stopped);
    }
  }

  private static List<Found> locate(
      final ServerWorld world,
      final List<RegistryEntry<Structure>> entries,
      final BlockPos from,
      final int range) {
    final StructurePlacementCalculator placements =
        world.getChunkManager().getStructurePlacementCalculator();
    final List<Found> found = new ArrayList<>();
    for (final RegistryEntry<Structure> entry : entries) {
      final Identifier id = entry.getKey().orElseThrow().getValue();
      final int reach = reach(range, entry.value() instanceof VampireStructure);
      for (final StructurePlacement placement : placements.getPlacements(entry)) {
        if (!(placement instanceof RandomSpreadStructurePlacement spread)) {
          Nightborn.LOGGER.warn("Structure search cannot walk the placement of {}", id);
          continue;
        }
        for (final ChunkPos probe :
            SpreadCells.probes(
                new ChunkPos(from), SpreadCells.chunks(reach), spread.getSpacing())) {
          if (Thread.currentThread().isInterrupted()) {
            throw new CancellationException("Server stopped");
          }
          final ChunkPos start =
              spread.getStartChunk(placements.getStructureSeed(), probe.x, probe.z);
          final BlockPos at = spread.getLocatePos(start);
          if (SpreadCells.within(from, at, reach)
              && spread.shouldGenerate(placements, start.x, start.z)) {
            entranceAt(world, entry.value(), id, start, at)
                .filter(entrance -> SpreadCells.within(from, entrance, range))
                .ifPresent(entrance -> found.add(new Found(id, entrance)));
          }
        }
      }
    }
    return found;
  }

  static int reach(final int range, final boolean house) {
    return house ? range + JigsawStructure.MAX_SIZE : range;
  }

  public static BlockPos entranceOfLocated(
      final ServerWorld world, final RegistryEntry<Structure> entry, final BlockPos locate) {
    final StructurePlacementCalculator placements =
        world.getChunkManager().getStructurePlacementCalculator();
    for (final StructurePlacement placement : placements.getPlacements(entry)) {
      if (placement instanceof RandomSpreadStructurePlacement spread) {
        final Optional<ChunkPos> start =
            SpreadStart.behind(spread, placements.getStructureSeed(), locate);
        if (start.isPresent()) {
          return entranceAt(
                  world,
                  entry.value(),
                  entry.getKey().orElseThrow().getValue(),
                  start.get(),
                  locate)
              .orElse(locate);
        }
      }
    }
    return locate;
  }

  private static Optional<BlockPos> entranceAt(
      final ServerWorld world,
      final Structure structure,
      final Identifier id,
      final ChunkPos start,
      final BlockPos locate) {
    final SelectiveNbtCollector scanned = SavedStarts.collector();
    world
        .getChunkManager()
        .chunkLoadingManager
        .getWorker()
        .scanChunk(start, scanned)
        .completeOnTimeout(null, SCAN_SECONDS, TimeUnit.SECONDS)
        .join();
    final NbtElement root = scanned.getRoot();
    final Optional<Boolean> saved =
        SavedStarts.has(root, id, SharedConstants.getGameVersion().getSaveVersion().getId());
    if (!(structure instanceof VampireStructure house)) {
      return StructureEntrance.found(
          saved,
          () -> Optional.of(locate),
          () ->
              EntrancePlan.plans(world, structure, start) ? Optional.of(locate) : Optional.empty(),
          locate);
    }
    return StructureEntrance.found(
        saved,
        () -> SavedStarts.start(root, id).flatMap(nbt -> EntrancePlan.ofSaved(world, house, nbt)),
        () -> EntrancePlan.of(world, house, start),
        locate);
  }
}
