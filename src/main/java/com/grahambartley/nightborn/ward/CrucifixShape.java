package com.grahambartley.nightborn.ward;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class CrucifixShape {

  static final List<Direction> ARM_AXES = List.of(Direction.EAST, Direction.SOUTH);
  static final int REACH = 2;

  private CrucifixShape() {}

  public static boolean centredAt(final BlockPos centre, final Predicate<BlockPos> material) {
    if (!material.test(centre)
        || !material.test(centre.up())
        || !material.test(centre.down())
        || material.test(centre.up(2))) {
      return false;
    }
    return ARM_AXES.stream().anyMatch(axis -> armsAlong(centre, axis, material));
  }

  public static List<BlockPos> centresTouching(final BlockPos changed) {
    final List<BlockPos> centres = new ArrayList<>();
    for (int dy = -REACH; dy <= 1; dy++) {
      for (int d = -REACH; d <= REACH; d++) {
        centres.add(changed.add(d, dy, 0));
        if (d != 0) {
          centres.add(changed.add(0, dy, d));
        }
      }
    }
    return centres;
  }

  public static Set<BlockPos> blocks(final BlockPos centre, final Predicate<BlockPos> material) {
    final Set<BlockPos> blocks = new HashSet<>(List.of(centre, centre.up(), centre.down()));
    for (final Direction axis : ARM_AXES) {
      if (armsAlong(centre, axis, material)) {
        blocks.add(centre.offset(axis));
        blocks.add(centre.offset(axis.getOpposite()));
      }
    }
    return blocks;
  }

  private static boolean armsAlong(
      final BlockPos centre, final Direction axis, final Predicate<BlockPos> material) {
    final Direction back = axis.getOpposite();
    return arm(centre.offset(axis), material)
        && arm(centre.offset(back), material)
        && !material.test(centre.offset(axis, REACH))
        && !material.test(centre.offset(back, REACH));
  }

  private static boolean arm(final BlockPos arm, final Predicate<BlockPos> material) {
    return material.test(arm) && !material.test(arm.up()) && !material.test(arm.down());
  }
}
