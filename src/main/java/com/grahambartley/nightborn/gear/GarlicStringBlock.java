package com.grahambartley.nightborn.gear;

import com.grahambartley.nightborn.decor.FacingShapes;
import com.grahambartley.nightborn.decor.WallMount;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.ward.Wards;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class GarlicStringBlock extends Block {

  public static final MapCodec<GarlicStringBlock> CODEC = createCodec(GarlicStringBlock::new);
  public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
  public static final BooleanProperty HANGING = Properties.HANGING;

  private static final Map<Direction, VoxelShape> ON_WALL =
      FacingShapes.fromNorth(Block.createCuboidShape(0, 0, 13, 16, 16, 16));
  private static final VoxelShape FROM_CEILING = Block.createCuboidShape(3, 0, 3, 13, 16, 13);

  public GarlicStringBlock(final Settings settings) {
    super(settings);
    setDefaultState(
        getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(HANGING, false));
  }

  @Override
  protected MapCodec<GarlicStringBlock> getCodec() {
    return CODEC;
  }

  @Override
  protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
    builder.add(FACING, HANGING);
  }

  public static Direction support(final BlockState state) {
    return state.get(HANGING) ? Direction.UP : state.get(FACING).getOpposite();
  }

  @Override
  protected VoxelShape getOutlineShape(
      final BlockState state,
      final BlockView world,
      final BlockPos pos,
      final ShapeContext context) {
    return state.get(HANGING) ? FROM_CEILING : ON_WALL.get(state.get(FACING));
  }

  @Override
  protected boolean canPlaceAt(final BlockState state, final WorldView world, final BlockPos pos) {
    final Direction support = support(state);
    return support == Direction.UP
        ? Block.sideCoversSmallSquare(world, pos.up(), Direction.DOWN)
        : WallMount.backed(world, pos, support);
  }

  @Override
  @Nullable
  public BlockState getPlacementState(final ItemPlacementContext context) {
    for (final Direction direction : context.getPlacementDirections()) {
      if (direction == Direction.DOWN) {
        continue;
      }
      final BlockState state =
          direction == Direction.UP
              ? getDefaultState()
                  .with(HANGING, true)
                  .with(FACING, context.getHorizontalPlayerFacing().getOpposite())
              : getDefaultState().with(FACING, direction.getOpposite());
      if (state.canPlaceAt(context.getWorld(), context.getBlockPos())) {
        return state;
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
    return WallMount.fell(state, support(state), direction, world, pos)
        ? Blocks.AIR.getDefaultState()
        : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
  }

  @Override
  protected void onEntityCollision(
      final BlockState state, final World world, final BlockPos pos, final Entity entity) {
    if (!world.isClient) {
      Wards.touch(entity, ServerConfigService.current().wards());
    }
  }

  @Override
  protected BlockState rotate(final BlockState state, final BlockRotation rotation) {
    return state.with(FACING, rotation.rotate(state.get(FACING)));
  }

  @Override
  protected BlockState mirror(final BlockState state, final BlockMirror mirror) {
    return state.rotate(mirror.getRotation(state.get(FACING)));
  }
}
