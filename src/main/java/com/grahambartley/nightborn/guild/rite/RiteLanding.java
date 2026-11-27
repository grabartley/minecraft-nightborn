package com.grahambartley.nightborn.guild.rite;

import com.grahambartley.nightborn.structure.StandingSpot;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public final class RiteLanding {

  private RiteLanding() {}

  public static Optional<BlockPos> choose(
      final BlockPos entrance, final int radius, final int rise, final Predicate<BlockPos> stands) {
    return BlockPos.stream(
            entrance.add(-radius, -rise, -radius), entrance.add(radius, rise, radius))
        .map(BlockPos::toImmutable)
        .sorted(
            Comparator.comparingDouble((BlockPos spot) -> spot.getSquaredDistance(entrance))
                .thenComparing(Comparator.naturalOrder()))
        .filter(stands)
        .findFirst();
  }

  public static boolean stands(
      final BlockState floor,
      final BlockState feet,
      final BlockState head,
      final Predicate<BlockState> harms) {
    return StandingSpot.fits(floor, feet, head) && Stream.of(floor, feet, head).noneMatch(harms);
  }
}
