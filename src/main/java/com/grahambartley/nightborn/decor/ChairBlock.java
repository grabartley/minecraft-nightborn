package com.grahambartley.nightborn.decor;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class ChairBlock extends HorizontalFacingBlock {

  public static final MapCodec<ChairBlock> CODEC = createCodec(ChairBlock::new);
  public static final double SEAT_HEIGHT = 0.5;

  private static final Map<Direction, VoxelShape> SHAPES =
      FacingShapes.fromNorth(
          VoxelShapes.union(
              Block.createCuboidShape(2, 0, 2, 14, 8, 14),
              Block.createCuboidShape(2, 8, 12, 14, 23, 14)));

  public ChairBlock(final Settings settings) {
    super(settings);
    this.setDefaultState(this.getStateManager().getDefaultState().with(FACING, Direction.NORTH));
  }

  @Override
  protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
    return CODEC;
  }

  @Override
  protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
    builder.add(FACING);
  }

  @Override
  public BlockState getPlacementState(final ItemPlacementContext context) {
    return this.getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite());
  }

  @Override
  protected VoxelShape getOutlineShape(
      final BlockState state,
      final BlockView world,
      final BlockPos pos,
      final ShapeContext context) {
    return SHAPES.get(state.get(FACING));
  }

  @Override
  protected ActionResult onUse(
      final BlockState state,
      final World world,
      final BlockPos pos,
      final PlayerEntity player,
      final BlockHitResult hit) {
    final Seating.Choice choice =
        Seating.choose(
            player.isSneaking(),
            player.hasVehicle(),
            !world.getEntitiesByClass(SeatEntity.class, new Box(pos), seat -> true).isEmpty());
    if (choice == Seating.Choice.PASS) {
      return ActionResult.PASS;
    }
    if (choice == Seating.Choice.TAKEN) {
      return ActionResult.CONSUME;
    }
    if (!world.isClient) {
      final SeatEntity seat = new SeatEntity(DecorEntities.SEAT, world);
      seat.refreshPositionAndAngles(
          pos.getX() + 0.5,
          pos.getY() + SEAT_HEIGHT,
          pos.getZ() + 0.5,
          state.get(FACING).asRotation(),
          0.0f);
      world.spawnEntity(seat);
      player.startRiding(seat);
      world.playSound(null, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 0.6f, 0.75f);
    }
    return ActionResult.success(world.isClient);
  }
}
