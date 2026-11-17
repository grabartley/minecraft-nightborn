package com.grahambartley.nightborn.structure;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public final class HouseholdSpots {

  private static final int ELBOW_ROOM = 2;

  private HouseholdSpots() {}

  public static List<BlockPos> choose(
      final List<BlockPos> spots, final int count, final Random random, final List<BlockPos> held) {
    final List<BlockPos> shuffled = Util.copyShuffled(spots.stream(), random);
    final List<BlockPos> chosen = new ArrayList<>();
    for (final BlockPos spot : shuffled) {
      if (chosen.size() < count
          && !held.contains(spot)
          && chosen.stream().noneMatch(taken -> crowds(taken, spot))
          && held.stream().noneMatch(taken -> crowds(taken, spot))) {
        chosen.add(spot);
      }
    }
    for (final BlockPos spot : shuffled) {
      if (chosen.size() < count && !chosen.contains(spot) && !held.contains(spot)) {
        chosen.add(spot);
      }
    }
    return List.copyOf(chosen);
  }

  public static Optional<BlockPos> middle(
      final List<BlockPos> spots, final BlockPos centre, final HeadSeat seat) {
    return spots.stream()
        .min(
            Comparator.comparingDouble(
                    (BlockPos spot) -> {
                      final double dx = spot.getX() - centre.getX();
                      final double dz = spot.getZ() - centre.getZ();
                      return dx * dx + dz * dz;
                    })
                .thenComparing(seat.order())
                .thenComparing(BlockPos::asLong));
  }

  private static boolean crowds(final BlockPos taken, final BlockPos spot) {
    return taken.getManhattanDistance(spot) < ELBOW_ROOM;
  }
}
