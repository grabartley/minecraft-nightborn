package com.grahambartley.nightborn.mob.goal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.util.math.Vec3i;

public final class ShadeSearch {

  static final Comparator<Vec3i> NEAREST_FIRST =
      Comparator.<Vec3i>comparingInt(
              offset -> offset.getX() * offset.getX() + offset.getZ() * offset.getZ())
          .thenComparingInt(offset -> Math.abs(offset.getY()))
          .thenComparingInt(offset -> -offset.getY())
          .thenComparingInt(Vec3i::getX)
          .thenComparingInt(Vec3i::getZ);

  private ShadeSearch() {}

  public static List<Vec3i> nearestFirst(final int radius, final int height) {
    final List<Vec3i> offsets = new ArrayList<>();
    for (int x = -radius; x <= radius; x++) {
      for (int z = -radius; z <= radius; z++) {
        for (int y = -height; y <= height; y++) {
          offsets.add(new Vec3i(x, y, z));
        }
      }
    }
    offsets.sort(NEAREST_FIRST);
    return List.copyOf(offsets);
  }

  public static <R> Optional<R> nearestReachable(
      final List<Vec3i> nearestFirst,
      final Predicate<Vec3i> shaded,
      final Set<Vec3i> ruledOut,
      final Function<List<Vec3i>, Optional<R>> route,
      final int batchSize,
      final int batches) {
    for (int batch = 0; batch < batches; batch++) {
      final List<Vec3i> spots = new ArrayList<>(batchSize);
      for (final Vec3i offset : nearestFirst) {
        if (spots.size() == batchSize) {
          break;
        }
        if (!ruledOut.contains(offset) && shaded.test(offset)) {
          spots.add(offset);
        }
      }
      if (spots.isEmpty()) {
        return Optional.empty();
      }
      final Optional<R> found = route.apply(List.copyOf(spots));
      if (found.isPresent()) {
        return found;
      }
      ruledOut.addAll(spots);
    }
    return Optional.empty();
  }
}
