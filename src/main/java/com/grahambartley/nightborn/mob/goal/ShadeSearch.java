package com.grahambartley.nightborn.mob.goal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.util.math.Vec3i;

public final class ShadeSearch {

  private static final Comparator<Vec3i> NEAREST_FIRST =
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
      final Function<Vec3i, Optional<R>> route,
      final int routeTries) {
    int tries = 0;
    for (final Vec3i offset : nearestFirst) {
      if (tries >= routeTries) {
        return Optional.empty();
      }
      if (!shaded.test(offset)) {
        continue;
      }
      tries++;
      final Optional<R> found = route.apply(offset);
      if (found.isPresent()) {
        return found;
      }
    }
    return Optional.empty();
  }
}
