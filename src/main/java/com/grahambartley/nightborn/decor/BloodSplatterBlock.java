package com.grahambartley.nightborn.decor;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class BloodSplatterBlock extends WallMountedBlock {

  public static final int SHAPES = 4;
  public static final IntProperty SPLAT = IntProperty.of("splat", 0, SHAPES - 1);
  public static final MapCodec<BloodSplatterBlock> CODEC = createCodec(BloodSplatterBlock::new);

  private static final VoxelShape ON_FLOOR = Block.createCuboidShape(0, 0, 0, 16, 1, 16);
  private static final VoxelShape ON_CEILING = Block.createCuboidShape(0, 15, 0, 16, 16, 16);
  private static final Map<Direction, VoxelShape> ON_WALL =
      FacingShapes.fromNorth(Block.createCuboidShape(0, 0, 15, 16, 16, 16));

  public BloodSplatterBlock(final Settings settings) {
    super(settings);
    this.setDefaultState(
        this.getStateManager()
            .getDefaultState()
            .with(FACING, Direction.NORTH)
            .with(FACE, BlockFace.FLOOR)
            .with(SPLAT, 0));
  }

  @Override
  protected MapCodec<BloodSplatterBlock> getCodec() {
    return CODEC;
  }

  @Override
  protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
    builder.add(FACING, FACE, SPLAT);
  }

  @Override
  protected VoxelShape getOutlineShape(
      final BlockState state,
      final BlockView world,
      final BlockPos pos,
      final ShapeContext context) {
    return switch (state.get(FACE)) {
      case FLOOR -> ON_FLOOR;
      case CEILING -> ON_CEILING;
      case WALL -> ON_WALL.get(state.get(FACING));
    };
  }

  @Override
  @Nullable
  public BlockState getPlacementState(final ItemPlacementContext context) {
    final BlockState state = super.getPlacementState(context);
    return state == null
        ? null
        : state.with(SPLAT, DecorVariants.pick(context.getBlockPos(), SHAPES));
  }
}
