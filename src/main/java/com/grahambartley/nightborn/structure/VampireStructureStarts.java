package com.grahambartley.nightborn.structure;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
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

  public static boolean contain(
      final ServerWorld world, final BlockPos pos, final Predicate<VampireStructure> which) {
    return loaded(world, pos.getX() >> 4, pos.getZ() >> 4)
        .map(chunk -> referencedBy(world, chunk))
        .orElse(List.of())
        .stream()
        .anyMatch(
            start ->
                which.test((VampireStructure) start.getStructure())
                    && start.getChildren().stream()
                        .anyMatch(piece -> piece.getBoundingBox().contains(pos)));
  }

  static Optional<Chunk> loaded(final ServerWorld world, final int x, final int z) {
    return Optional.ofNullable(world.getChunkManager().getWorldChunk(x, z));
  }
}
