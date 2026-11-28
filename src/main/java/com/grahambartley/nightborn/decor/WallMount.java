package com.grahambartley.nightborn.decor;

import java.util.Arrays;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public final class WallMount {

  private WallMount() {}

  public static List<Direction> facings(final Direction[] looked) {
    return Arrays.stream(looked)
        .filter(direction -> direction.getAxis().isHorizontal())
        .map(Direction::getOpposite)
        .toList();
  }

  public static boolean backed(final WorldView world, final BlockPos pos, final Direction support) {
    final BlockPos behind = pos.offset(support);
    return world.getBlockState(behind).isSideSolidFullSquare(world, behind, support.getOpposite());
  }

  @Nullable
  public static BlockState placed(
      final BlockState base, final DirectionProperty facing, final ItemPlacementContext context) {
    for (final Direction towards : facings(context.getPlacementDirections())) {
      final BlockState state = base.with(facing, towards);
      if (state.canPlaceAt(context.getWorld(), context.getBlockPos())) {
        return state;
      }
    }
    return null;
  }

  public static boolean fell(
      final BlockState state,
      final Direction support,
      final Direction changed,
      final WorldView world,
      final BlockPos pos) {
    return changed == support && !state.canPlaceAt(world, pos);
  }
}
