package com.grahambartley.nightborn.decor;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class StatueBlock extends HorizontalFacingBlock {

  private final Map<Direction, VoxelShape> shapes;
  private final MapCodec<StatueBlock> codec;

  public StatueBlock(final Settings settings, final VoxelShape northShape) {
    super(settings);
    this.shapes = FacingShapes.fromNorth(northShape);
    this.codec = createCodec(copy -> new StatueBlock(copy, northShape));
    this.setDefaultState(this.getStateManager().getDefaultState().with(FACING, Direction.NORTH));
  }

  @Override
  protected MapCodec<StatueBlock> getCodec() {
    return this.codec;
  }

  @Override
  protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
    builder.add(FACING);
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
  public BlockState getPlacementState(final ItemPlacementContext context) {
    return this.getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite());
  }
}
