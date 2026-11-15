package com.grahambartley.nightborn.decor;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class WallDecorBlock extends HorizontalFacingBlock {

  private final Map<Direction, VoxelShape> shapes;
  private final MapCodec<WallDecorBlock> codec;

  public WallDecorBlock(final Settings settings, final VoxelShape northShape) {
    super(settings);
    this.shapes = FacingShapes.fromNorth(northShape);
    this.codec = createCodec(copy -> new WallDecorBlock(copy, northShape));
    this.setDefaultState(this.getStateManager().getDefaultState().with(FACING, Direction.NORTH));
  }

  @Override
  protected MapCodec<? extends WallDecorBlock> getCodec() {
    return this.codec;
  }

  @Override
  protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
    builder.add(FACING);
  }

  public static Direction support(final BlockState state) {
    return state.get(FACING).getOpposite();
  }

  @Override
  protected VoxelShape getOutlineShape(
      final BlockState state,
      final BlockView world,
      final BlockPos pos,
      final ShapeContext context) {
    return this.shapes.get(state.get(FACING));
  }

  @Override
  protected boolean canPlaceAt(final BlockState state, final WorldView world, final BlockPos pos) {
    final Direction support = support(state);
    final BlockPos behind = pos.offset(support);
    return world.getBlockState(behind).isSideSolidFullSquare(world, behind, support.getOpposite());
  }

  @Override
  @Nullable
  public BlockState getPlacementState(final ItemPlacementContext context) {
    for (final Direction direction : context.getPlacementDirections()) {
      if (direction.getAxis().isHorizontal()) {
        final BlockState state = this.getDefaultState().with(FACING, direction.getOpposite());
        if (state.canPlaceAt(context.getWorld(), context.getBlockPos())) {
          return state;
        }
      }
    }
    return null;
  }

  @Override
  protected BlockState getStateForNeighborUpdate(
      final BlockState state,
      final Direction direction,
      final BlockState neighborState,
      final WorldAccess world,
      final BlockPos pos,
      final BlockPos neighborPos) {
    return direction == support(state) && !state.canPlaceAt(world, pos)
        ? Blocks.AIR.getDefaultState()
        : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
  }
}
