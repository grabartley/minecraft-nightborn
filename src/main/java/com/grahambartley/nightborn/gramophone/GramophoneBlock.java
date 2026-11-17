package com.grahambartley.nightborn.gramophone;

import com.grahambartley.nightborn.decor.DecorBlockEntities;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class GramophoneBlock extends BlockWithEntity {

  public static final MapCodec<GramophoneBlock> CODEC = createCodec(GramophoneBlock::new);
  public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
  public static final BooleanProperty HAS_RECORD = Properties.HAS_RECORD;
  public static final BooleanProperty PLAYING = BooleanProperty.of("playing");
  static final int PLAYING_POWER = 15;
  private static final VoxelShape SHAPE = Block.createCuboidShape(1, 0, 1, 15, 6, 15);

  public GramophoneBlock(final Settings settings) {
    super(settings);
    this.setDefaultState(
        this.getDefaultState()
            .with(FACING, Direction.NORTH)
            .with(HAS_RECORD, false)
            .with(PLAYING, false));
  }

  @Override
  protected MapCodec<GramophoneBlock> getCodec() {
    return CODEC;
  }

  @Override
  protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
    builder.add(FACING, HAS_RECORD, PLAYING);
  }

  @Override
  public BlockState getPlacementState(final ItemPlacementContext context) {
    return this.getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite());
  }

  @Override
  public void onPlaced(
      final World world,
      final BlockPos pos,
      final BlockState state,
      final LivingEntity placer,
      final ItemStack stack) {
    super.onPlaced(world, pos, state, placer, stack);
    if (stack
        .getOrDefault(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.DEFAULT)
        .contains(GramophoneBlockEntity.RECORD_KEY)) {
      world.setBlockState(pos, state.with(HAS_RECORD, true), Block.NOTIFY_LISTENERS);
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

  @Override
  protected VoxelShape getOutlineShape(
      final BlockState state,
      final BlockView world,
      final BlockPos pos,
      final ShapeContext context) {
    return SHAPE;
  }

  @Override
  protected boolean canPathfindThrough(final BlockState state, final NavigationType type) {
    return false;
  }

  @Override
  protected BlockRenderType getRenderType(final BlockState state) {
    return BlockRenderType.ENTITYBLOCK_ANIMATED;
  }

  @Override
  public BlockEntity createBlockEntity(final BlockPos pos, final BlockState state) {
    return new GramophoneBlockEntity(pos, state);
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      final World world, final BlockState state, final BlockEntityType<T> type) {
    return world.isClient || !state.get(HAS_RECORD)
        ? null
        : validateTicker(type, DecorBlockEntities.GRAMOPHONE, GramophoneBlockEntity::tick);
  }

  @Override
  protected ItemActionResult onUseWithItem(
      final ItemStack stack,
      final BlockState state,
      final World world,
      final BlockPos pos,
      final PlayerEntity player,
      final Hand hand,
      final BlockHitResult hit) {
    if (!use(state, stack).equals(Optional.of(GramophoneUse.INSERT))
        || !(world.getBlockEntity(pos) instanceof GramophoneBlockEntity gramophone)) {
      return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    if (!world.isClient) {
      gramophone.setStack(stack.splitUnlessCreative(1, player));
      world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, state));
      player.incrementStat(Stats.PLAY_RECORD);
    }
    return ItemActionResult.success(world.isClient);
  }

  @Override
  protected ActionResult onUse(
      final BlockState state,
      final World world,
      final BlockPos pos,
      final PlayerEntity player,
      final BlockHitResult hit) {
    if (!use(state, ItemStack.EMPTY).equals(Optional.of(GramophoneUse.EJECT))
        || !(world.getBlockEntity(pos) instanceof GramophoneBlockEntity gramophone)) {
      return ActionResult.PASS;
    }
    gramophone.dropRecord();
    return ActionResult.success(world.isClient);
  }

  @Override
  protected void onStateReplaced(
      final BlockState state,
      final World world,
      final BlockPos pos,
      final BlockState newState,
      final boolean moved) {
    if (state.isOf(newState.getBlock())) {
      return;
    }
    if (world.getBlockEntity(pos) instanceof GramophoneBlockEntity gramophone) {
      gramophone.dropRecord();
    }
    super.onStateReplaced(state, world, pos, newState, moved);
  }

  @Override
  protected boolean emitsRedstonePower(final BlockState state) {
    return true;
  }

  @Override
  protected int getWeakRedstonePower(
      final BlockState state,
      final BlockView world,
      final BlockPos pos,
      final Direction direction) {
    return world.getBlockEntity(pos) instanceof GramophoneBlockEntity gramophone
            && gramophone.playing()
        ? PLAYING_POWER
        : 0;
  }

  @Override
  protected boolean hasComparatorOutput(final BlockState state) {
    return true;
  }

  @Override
  protected int getComparatorOutput(final BlockState state, final World world, final BlockPos pos) {
    return world.getBlockEntity(pos) instanceof GramophoneBlockEntity gramophone
        ? gramophone.comparatorOutput()
        : 0;
  }

  private static Optional<GramophoneUse> use(final BlockState state, final ItemStack held) {
    return GramophoneUse.decide(
        state.get(HAS_RECORD), held.contains(DataComponentTypes.JUKEBOX_PLAYABLE));
  }
}
