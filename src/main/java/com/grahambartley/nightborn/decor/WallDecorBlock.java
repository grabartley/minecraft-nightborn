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
    return WallMount.backed(world, pos, support(state));
  }

  @Override
  @Nullable
  public BlockState getPlacementState(final ItemPlacementContext context) {
    return WallMount.placed(this.getDefaultState(), FACING, context);
  }

  @Override
  protected BlockState getStateForNeighborUpdate(
      final BlockState state,
      final Direction direction,
      final BlockState neighborState,
      final WorldAccess world,
      final BlockPos pos,
      final BlockPos neighborPos) {
    return WallMount.fell(state, support(state), direction, world, pos)
        ? Blocks.AIR.getDefaultState()
        : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
  }
}
