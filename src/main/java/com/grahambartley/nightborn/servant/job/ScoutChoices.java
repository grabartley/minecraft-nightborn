package com.grahambartley.nightborn.servant.job;

import com.grahambartley.nightborn.structure.NearestStructures.Found;
import java.util.Comparator;
import java.util.List;
import net.minecraft.util.math.BlockPos;

public final class ScoutChoices {

  private ScoutChoices() {}

  public static List<Found> nearest(
      final BlockPos from, final List<Found> found, final int range, final int most) {
    final double reach = (double) range * range;
    return found.stream()
        .filter(place -> flatDistanceSquared(from, place.pos()) <= reach)
        .distinct()
        .sorted(Comparator.comparingDouble(place -> flatDistanceSquared(from, place.pos())))
        .limit(most)
        .toList();
  }

  public static double flatDistanceSquared(final BlockPos from, final BlockPos to) {
    final double dx = to.getX() - from.getX();
    final double dz = to.getZ() - from.getZ();
    return dx * dx + dz * dz;
  }
}
