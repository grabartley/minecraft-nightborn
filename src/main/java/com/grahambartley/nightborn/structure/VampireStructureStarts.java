package com.grahambartley.nightborn.structure;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.structure.Structure;

public final class VampireStructureStarts {

  private VampireStructureStarts() {}

  public static List<StructureStart> referencedBy(final ServerWorld world, final Chunk chunk) {
    final List<StructureStart> starts = new ArrayList<>();
    for (final Map.Entry<Structure, LongSet> entry : chunk.getStructureReferences().entrySet()) {
      if (entry.getKey().getType() != NightbornStructures.VAMPIRE_STRUCTURE) {
        continue;
      }
      for (final long reference : entry.getValue()) {
        final ChunkPos home = new ChunkPos(reference);
        loaded(world, home.x, home.z)
            .map(startChunk -> startChunk.getStructureStart(entry.getKey()))
            .filter(StructureStart::hasChildren)
            .ifPresent(starts::add);
      }
    }
    return starts;
  }

  public static boolean anyContains(
      final ServerWorld world, final BlockPos pos, final Predicate<VampireStructure> which) {
    return containing(world, pos)
        .anyMatch(start -> which.test((VampireStructure) start.getStructure()));
  }

  public static Optional<BlockPos> homeContaining(final ServerWorld world, final BlockPos pos) {
    return containing(world, pos)
        .flatMap(start -> StructureHeart.of(start.getChildren()).stream())
        .map(BlockBox::getCenter)
        .findFirst();
  }

  public static Optional<StructureStart> startAt(final ServerWorld world, final BlockPos home) {
    return near(world, home)
        .filter(
            start ->
                StructureHeart.of(start.getChildren())
                    .filter(heart -> heart.getCenter().equals(home))
                    .isPresent())
        .findFirst();
  }

  public static boolean whollyLoaded(final ServerWorld world, final BlockBox box) {
    return chunksUnder(box).stream().allMatch(chunk -> loaded(world, chunk.x, chunk.z).isPresent());
  }

  static List<ChunkPos> chunksUnder(final BlockBox box) {
    final List<ChunkPos> chunks = new ArrayList<>();
    for (int x = box.getMinX() >> 4; x <= box.getMaxX() >> 4; x++) {
      for (int z = box.getMinZ() >> 4; z <= box.getMaxZ() >> 4; z++) {
        chunks.add(new ChunkPos(x, z));
      }
    }
    return chunks;
  }

  static boolean anyHolds(final List<BlockBox> pieces, final BlockPos pos) {
    return pieces.stream().anyMatch(box -> box.contains(pos));
  }

  private static Stream<StructureStart> containing(final ServerWorld world, final BlockPos pos) {
    return near(world, pos)
        .filter(
            start ->
                anyHolds(
                    start.getChildren().stream().map(piece -> piece.getBoundingBox()).toList(),
                    pos));
  }

  private static Stream<StructureStart> near(final ServerWorld world, final BlockPos pos) {
    return loaded(world, pos.getX() >> 4, pos.getZ() >> 4).stream()
        .flatMap(chunk -> referencedBy(world, chunk).stream());
  }

  private static Optional<Chunk> loaded(final ServerWorld world, final int x, final int z) {
    return Optional.ofNullable(world.getChunkManager().getWorldChunk(x, z));
  }
}
