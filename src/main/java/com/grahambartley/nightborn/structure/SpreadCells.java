package com.grahambartley.nightborn.structure;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public final class SpreadCells {

  public static final int CHUNK_BLOCKS = 16;

  private SpreadCells() {}

  public static int chunks(final int blocks) {
    return Math.max(1, Math.ceilDiv(blocks, CHUNK_BLOCKS));
  }

  public static List<ChunkPos> probes(final ChunkPos center, final int chunks, final int spacing) {
    final int fromX = Math.floorDiv(center.x - chunks, spacing);
    final int toX = Math.floorDiv(center.x + chunks, spacing);
    final int fromZ = Math.floorDiv(center.z - chunks, spacing);
    final int toZ = Math.floorDiv(center.z + chunks, spacing);
    final List<ChunkPos> probes = new ArrayList<>();
    for (int regionX = fromX; regionX <= toX; regionX++) {
      for (int regionZ = fromZ; regionZ <= toZ; regionZ++) {
        probes.add(new ChunkPos(regionX * spacing, regionZ * spacing));
      }
    }
    return probes;
  }

  public static boolean within(final BlockPos from, final BlockPos to, final int blocks) {
    final long dx = to.getX() - from.getX();
    final long dz = to.getZ() - from.getZ();
    return dx * dx + dz * dz <= (long) blocks * blocks;
  }
}
