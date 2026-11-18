package com.grahambartley.nightborn.structure;

import java.util.List;
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;

public final class LocateRadius {

  public static final int CHUNK_BLOCKS = 16;

  private LocateRadius() {}

  public static int chunks(final int blocks) {
    return Math.max(1, Math.ceilDiv(blocks, CHUNK_BLOCKS));
  }

  public static int of(final int blocks, final List<StructurePlacement> placements) {
    final int chunks = chunks(blocks);
    return placements.stream()
        .filter(RandomSpreadStructurePlacement.class::isInstance)
        .mapToInt(placement -> ((RandomSpreadStructurePlacement) placement).getSpacing())
        .min()
        .stream()
        .map(spacing -> Math.max(1, Math.ceilDiv(chunks, spacing)))
        .findFirst()
        .orElse(chunks);
  }
}
