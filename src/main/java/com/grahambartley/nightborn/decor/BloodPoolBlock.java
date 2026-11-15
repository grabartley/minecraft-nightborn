package com.grahambartley.nightborn.decor;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class BloodPoolBlock extends Block {

  public static final MapCodec<BloodPoolBlock> CODEC = createCodec(BloodPoolBlock::new);
  private static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 1, 16);

  public BloodPoolBlock(final Settings settings) {
    super(settings);
  }

  @Override
  protected MapCodec<BloodPoolBlock> getCodec() {
    return CODEC;
  }

  @Override
  protected VoxelShape getOutlineShape(
      final BlockState state,
      final BlockView world,
      final BlockPos pos,
      final ShapeContext context) {
    return SHAPE;
  }

  @Override
  protected boolean canPlaceAt(final BlockState state, final WorldView world, final BlockPos pos) {
    final BlockPos below = pos.down();
    return world.getBlockState(below).isSideSolidFullSquare(world, below, Direction.UP);
  }

  @Override
  protected BlockState getStateForNeighborUpdate(
      final BlockState state,
      final Direction direction,
      final BlockState neighborState,
      final WorldAccess world,
      final BlockPos pos,
      final BlockPos neighborPos) {
    return direction == Direction.DOWN && !state.canPlaceAt(world, pos)
        ? Blocks.AIR.getDefaultState()
        : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
  }
}
