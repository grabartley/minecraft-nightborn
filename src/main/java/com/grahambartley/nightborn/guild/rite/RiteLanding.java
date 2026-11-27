package com.grahambartley.nightborn.guild.rite;

import com.grahambartley.nightborn.structure.StandingSpot;
import com.grahambartley.nightborn.structure.Threshold;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class RiteLanding {

  private RiteLanding() {}

  public static Optional<BlockPos> choose(
      final Threshold door, final int radius, final int rise, final Predicate<BlockPos> stands) {
    final BlockPos before = door.step().up();
    return BlockPos.stream(before.add(-radius, -rise, -radius), before.add(radius, rise, radius))
        .map(BlockPos::toImmutable)
        .filter(spot -> outside(door, spot))
        .sorted(
            Comparator.comparingDouble((BlockPos spot) -> spot.getSquaredDistance(before))
                .thenComparing(Comparator.naturalOrder()))
        .filter(stands)
        .findFirst();
  }

  public static boolean outside(final Threshold door, final BlockPos spot) {
    final Direction out = door.out();
    final BlockPos floor = door.floor();
    return (spot.getX() - floor.getX()) * out.getOffsetX()
            + (spot.getZ() - floor.getZ()) * out.getOffsetZ()
        >= 1;
  }

  public static boolean stands(
      final BlockState floor,
      final BlockState feet,
      final BlockState head,
      final Predicate<BlockState> harms) {
    return StandingSpot.fits(floor, feet, head) && Stream.of(floor, feet, head).noneMatch(harms);
  }
}
