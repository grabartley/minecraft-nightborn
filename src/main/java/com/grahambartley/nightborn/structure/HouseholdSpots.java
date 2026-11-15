package com.grahambartley.nightborn.structure;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public final class HouseholdSpots {

  private static final int ELBOW_ROOM = 2;

  private HouseholdSpots() {}

  public static List<BlockPos> choose(
      final List<BlockPos> spots, final int count, final Random random) {
    final List<BlockPos> shuffled = Util.copyShuffled(spots.stream(), random);
    final List<BlockPos> chosen = new ArrayList<>();
    for (final BlockPos spot : shuffled) {
      if (chosen.size() < count && chosen.stream().noneMatch(taken -> crowds(taken, spot))) {
        chosen.add(spot);
      }
    }
    for (final BlockPos spot : shuffled) {
      if (chosen.size() < count && !chosen.contains(spot)) {
        chosen.add(spot);
      }
    }
    return List.copyOf(chosen);
  }

  private static boolean crowds(final BlockPos taken, final BlockPos spot) {
    return taken.getManhattanDistance(spot) < ELBOW_ROOM;
  }
}
