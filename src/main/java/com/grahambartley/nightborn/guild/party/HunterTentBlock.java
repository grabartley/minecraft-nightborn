package com.grahambartley.nightborn.guild.party;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class HunterTentBlock extends BlockWithEntity {

  public static final MapCodec<HunterTentBlock> CODEC = createCodec(HunterTentBlock::new);
  public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
  public static final EnumProperty<TentPart> PART = EnumProperty.of("part", TentPart.class);
  public static final BooleanProperty STRUCK = BooleanProperty.of("struck");
  public static final int STRIKE_TICKS = 20;

  public HunterTentBlock(final Settings settings) {
    super(settings);
    setDefaultState(
        getDefaultState()
            .with(FACING, Direction.NORTH)
            .with(PART, TentPart.ORIGIN)
            .with(STRUCK, false));
  }

  public static void strike(final ServerWorld world, final BlockPos origin) {
    final BlockState state = world.getBlockState(origin);
    if (!(state.getBlock() instanceof HunterTentBlock tent)
        || state.get(PART) != TentPart.ORIGIN
        || state.get(STRUCK)) {
      return;
    }
    world.setBlockState(origin, state.with(STRUCK, true), Block.NOTIFY_LISTENERS);
    world.playSound(null, origin, SoundEvents.BLOCK_WOOL_BREAK, SoundCategory.BLOCKS, 1.0F, 0.8F);
    world.scheduleBlockTick(origin, tent, STRIKE_TICKS);
  }

  @Override
  protected MapCodec<HunterTentBlock> getCodec() {
    return CODEC;
  }

  @Override
  protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
    builder.add(FACING, PART, STRUCK);
  }

  @Override
  protected VoxelShape getOutlineShape(
      final BlockState state,
      final BlockView world,
      final BlockPos pos,
      final ShapeContext context) {
    return TentShapes.outline(state.get(PART), state.get(FACING));
  }

  @Override
  protected VoxelShape getCollisionShape(
      final BlockState state,
      final BlockView world,
      final BlockPos pos,
      final ShapeContext context) {
    return TentShapes.collision(state.get(PART), state.get(FACING));
  }

  @Override
  protected BlockRenderType getRenderType(final BlockState state) {
    return state.get(PART) == TentPart.ORIGIN
        ? BlockRenderType.ENTITYBLOCK_ANIMATED
        : BlockRenderType.INVISIBLE;
  }

  @Nullable
  @Override
  public BlockEntity createBlockEntity(final BlockPos pos, final BlockState state) {
    return state.get(PART) == TentPart.ORIGIN ? new HunterTentBlockEntity(pos, state) : null;
  }

  @Override
  protected BlockState rotate(final BlockState state, final BlockRotation rotation) {
    return state.with(FACING, rotation.rotate(state.get(FACING)));
  }

  @Override
  protected BlockState mirror(final BlockState state, final BlockMirror mirror) {
    return state.rotate(mirror.getRotation(state.get(FACING)));
  }

  @Override
  protected void onBlockAdded(
      final BlockState state,
      final World world,
      final BlockPos pos,
      final BlockState oldState,
      final boolean notify) {
    if (!world.isClient && state.get(PART) == TentPart.ORIGIN && !oldState.isOf(this)) {
      world.playSound(null, pos, SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.BLOCKS, 1.0F, 0.8F);
    }
  }

  @Override
  protected void scheduledTick(
      final BlockState state, final ServerWorld world, final BlockPos pos, final Random random) {
    if (state.get(STRUCK)) {
      world.removeBlock(pos, false);
    }
  }

  @Override
  protected void onStateReplaced(
      final BlockState state,
      final World world,
      final BlockPos pos,
      final BlockState newState,
      final boolean moved) {
    if (!state.isOf(newState.getBlock()) && world instanceof ServerWorld server) {
      CampClaims.of(server).release(pos, state);
      takeDown(server, pos, state);
    }
    super.onStateReplaced(state, world, pos, newState, moved);
  }

  private void takeDown(final ServerWorld world, final BlockPos pos, final BlockState state) {
    final Direction facing = state.get(FACING);
    final BlockPos origin = state.get(PART).origin(pos, facing);
    for (final TentPart part : TentPart.values()) {
      final BlockPos at = origin.add(part.offset(facing));
      final BlockState there = world.getBlockState(at);
      if (there.isOf(this) && there.get(FACING) == facing && there.get(PART) == part) {
        world.setBlockState(at, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
      }
    }
  }
}
