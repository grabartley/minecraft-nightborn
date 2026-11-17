package com.grahambartley.nightborn.ritual;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class SummonSpot {

  static final int REACH = 2;

  private SummonSpot() {}

  public static List<BlockPos> candidates(final BlockPos altar, final Direction facing) {
    final List<BlockPos> spots = new ArrayList<>();
    final Direction[] sides = {
      facing, facing.rotateYClockwise(), facing.rotateYCounterclockwise(), facing.getOpposite()
    };
    for (int distance = REACH; distance >= 1; distance--) {
      for (final Direction side : sides) {
        for (int rise = 0; rise >= -1; rise--) {
          spots.add(altar.offset(side, distance).up(rise));
        }
      }
    }
    return spots;
  }

  public static BlockPos pick(
      final BlockPos altar, final Direction facing, final Predicate<BlockPos> fits) {
    return candidates(altar, facing).stream().filter(fits).findFirst().orElse(altar.up());
  }
}
