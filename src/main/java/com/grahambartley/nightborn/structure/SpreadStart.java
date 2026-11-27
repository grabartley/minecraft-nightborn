package com.grahambartley.nightborn.structure;

import java.util.Optional;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement;

public final class SpreadStart {

  private SpreadStart() {}

  public static Optional<ChunkPos> behind(
      final RandomSpreadStructurePlacement spread, final long seed, final BlockPos locate) {
    final ChunkPos near = new ChunkPos(locate);
    final ChunkPos start = spread.getStartChunk(seed, near.x, near.z);
    return spread.getLocatePos(start).equals(locate) ? Optional.of(start) : Optional.empty();
  }
}
