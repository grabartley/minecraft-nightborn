package com.grahambartley.nightborn.structure;

import net.minecraft.world.gen.StructureTerrainAdaptation;
import net.minecraft.world.gen.structure.JigsawStructure;

public final class StructureReach {

  private static final int ADAPTATION_PADDING = 12;

  private StructureReach() {}

  public static int padding(final StructureTerrainAdaptation adaptation) {
    return adaptation == StructureTerrainAdaptation.NONE ? 0 : ADAPTATION_PADDING;
  }

  public static boolean fits(
      final int maxDistanceFromCenter, final StructureTerrainAdaptation adaptation) {
    return maxDistanceFromCenter + padding(adaptation) <= JigsawStructure.MAX_SIZE;
  }
}
