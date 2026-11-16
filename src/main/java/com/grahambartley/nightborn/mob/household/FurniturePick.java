package com.grahambartley.nightborn.mob.household;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class FurniturePick {

  private FurniturePick() {}

  public static Optional<BlockPos> nearest(
      final List<BlockPos> spots, final Vec3d from, final Predicate<BlockPos> usable) {
    return spots.stream()
        .filter(usable)
        .min(Comparator.comparingDouble(spot -> Vec3d.ofCenter(spot).squaredDistanceTo(from)));
  }

  public static Optional<BlockPos> any(
      final List<BlockPos> spots, final Predicate<BlockPos> usable, final int roll) {
    final List<BlockPos> usableSpots = spots.stream().filter(usable).toList();
    return usableSpots.isEmpty()
        ? Optional.empty()
        : Optional.of(usableSpots.get(Math.floorMod(roll, usableSpots.size())));
  }
}
