package com.grahambartley.nightborn.decor;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class BloodFountainBlock extends FurnitureBlock {

  public static final BooleanProperty FLOWING = BooleanProperty.of("flowing");
  public static final MapCodec<BloodFountainBlock> CODEC = createCodec(BloodFountainBlock::new);

  private static final float AMBIENT_CHANCE = 0.12f;
  private static final VoxelShape SHAPE =
      VoxelShapes.union(
          Block.createCuboidShape(1, 0, 1, 15, 5, 15),
          Block.createCuboidShape(6.5, 5, 6.5, 9.5, 10, 9.5),
          Block.createCuboidShape(4, 10, 4, 12, 13, 12),
          Block.createCuboidShape(7, 13, 7, 9, 15, 9));

  public BloodFountainBlock(final Settings settings) {
    super(settings);
    this.setDefaultState(this.getDefaultState().with(FLOWING, true));
  }

  public static boolean flowsWhen(final boolean powered) {
    return !powered;
  }

  @Override
  protected MapCodec<? extends BlockWithEntity> getCodec() {
    return CODEC;
  }

  @Override
  public BooleanProperty toggleProperty() {
    return FLOWING;
  }

  @Override
  public String artModel() {
    return "blood_fountain";
  }

  @Override
  protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
    super.appendProperties(builder);
    builder.add(FLOWING);
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
  public BlockState getPlacementState(final ItemPlacementContext context) {
    return this.facingPlacer(context)
        .with(
            FLOWING, flowsWhen(context.getWorld().isReceivingRedstonePower(context.getBlockPos())));
  }

  @Override
  protected void neighborUpdate(
      final BlockState state,
      final World world,
      final BlockPos pos,
      final Block sourceBlock,
      final BlockPos sourcePos,
      final boolean notify) {
    if (world.isClient) {
      return;
    }
    final boolean flowing = flowsWhen(world.isReceivingRedstonePower(pos));
    if (flowing != state.get(FLOWING)) {
      world.setBlockState(pos, state.with(FLOWING, flowing), Block.NOTIFY_ALL);
      world.playSound(
          null,
          pos,
          flowing ? SoundEvents.ITEM_BUCKET_EMPTY : SoundEvents.ITEM_BUCKET_FILL,
          SoundCategory.BLOCKS,
          0.8f,
          0.7f);
      world.emitGameEvent(null, GameEvent.BLOCK_CHANGE, pos);
    }
  }

  @Override
  public void randomDisplayTick(
      final BlockState state, final World world, final BlockPos pos, final Random random) {
    if (state.get(FLOWING) && random.nextFloat() < AMBIENT_CHANCE) {
      world.playSound(
          pos.getX() + 0.5,
          pos.getY() + 0.5,
          pos.getZ() + 0.5,
          SoundEvents.BLOCK_WATER_AMBIENT,
          SoundCategory.BLOCKS,
          0.25f + random.nextFloat() * 0.1f,
          0.55f + random.nextFloat() * 0.1f,
          false);
    }
  }
}
