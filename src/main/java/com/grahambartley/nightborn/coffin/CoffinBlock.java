package com.grahambartley.nightborn.coffin;

import com.grahambartley.nightborn.vampire.Vampires;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;

public class CoffinBlock extends BedBlock {

  public static final MapCodec<BedBlock> CODEC = createCodec(CoffinBlock::new);
  public static final BooleanProperty OPEN = Properties.OPEN;

  static final float LID_PITCH = 0.55F;

  public CoffinBlock(final Settings settings) {
    super(DyeColor.BLACK, settings);
    this.setDefaultState(this.getDefaultState().with(OPEN, false));
  }

  @Override
  public MapCodec<BedBlock> getCodec() {
    return CODEC;
  }

  @Override
  protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
    super.appendProperties(builder);
    builder.add(OPEN);
  }

  @Override
  protected BlockRenderType getRenderType(final BlockState state) {
    return BlockRenderType.MODEL;
  }

  @Override
  protected VoxelShape getOutlineShape(
      final BlockState state,
      final BlockView world,
      final BlockPos pos,
      final ShapeContext context) {
    return CoffinShape.of(state.get(PART), state.get(FACING));
  }

  @Override
  public BlockEntity createBlockEntity(final BlockPos pos, final BlockState state) {
    return state.get(PART) == BedPart.HEAD ? new CoffinBlockEntity(pos, state) : null;
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      final World world, final BlockState state, final BlockEntityType<T> type) {
    if (!world.isClient || type != Coffins.BLOCK_ENTITY) {
      return null;
    }
    return (tickWorld, tickPos, tickState, entity) ->
        ((CoffinBlockEntity) entity).clientTick(tickState);
  }

  @Override
  protected ActionResult onUse(
      final BlockState state,
      final World world,
      final BlockPos pos,
      final PlayerEntity player,
      final BlockHitResult hit) {
    if (world.isClient) {
      return ActionResult.CONSUME;
    }
    final BlockPos head = state.get(PART) == BedPart.HEAD ? pos : pos.offset(state.get(FACING));
    final BlockState headState = world.getBlockState(head);
    if (!headState.isOf(this)) {
      return ActionResult.CONSUME;
    }
    switch (CoffinUse.of(
        headState.get(OCCUPIED), player.isSneaking(), Vampires.isVampire(player))) {
      case REFUSE_OCCUPIED -> player.sendMessage(Text.translatable(RestRules.OCCUPIED), true);
      case TOGGLE_LID -> this.setLid(world, head, headState, !headState.get(OPEN), player);
      case REST ->
          player
              .trySleep(head)
              .ifLeft(
                  reason ->
                      RestRules.message(reason)
                          .ifPresent(key -> player.sendMessage(Text.translatable(key), true)))
              .ifRight(rested -> player.extinguish());
    }
    return ActionResult.SUCCESS;
  }

  @Override
  protected void onBlockAdded(
      final BlockState state,
      final World world,
      final BlockPos pos,
      final BlockState oldState,
      final boolean notify) {
    if (state.get(PART) != BedPart.HEAD || !oldState.isOf(this)) {
      return;
    }
    final boolean open =
        CoffinLid.openAfter(state.get(OPEN), oldState.get(OCCUPIED), state.get(OCCUPIED));
    if (open != state.get(OPEN)) {
      this.setLid(world, pos, state, open, null);
    }
  }

  @Override
  protected BlockState getStateForNeighborUpdate(
      final BlockState state,
      final Direction direction,
      final BlockState neighborState,
      final WorldAccess world,
      final BlockPos pos,
      final BlockPos neighborPos) {
    final BlockState updated =
        super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    if (updated.isOf(this)
        && neighborState.isOf(this)
        && direction == getOppositePartDirection(state)
        && neighborState.get(PART) != state.get(PART)) {
      return updated.with(OPEN, neighborState.get(OPEN));
    }
    return updated;
  }

  @Override
  public void onLandedUpon(
      final World world,
      final BlockState state,
      final BlockPos pos,
      final Entity entity,
      final float fallDistance) {
    entity.handleFallDamage(fallDistance, 1.0F, entity.getDamageSources().fall());
  }

  @Override
  public void onEntityLand(final BlockView world, final Entity entity) {
    entity.setVelocity(entity.getVelocity().multiply(1.0, 0.0, 1.0));
  }

  private void setLid(
      final World world,
      final BlockPos head,
      final BlockState state,
      final boolean open,
      final PlayerEntity player) {
    world.setBlockState(head, state.with(OPEN, open), Block.NOTIFY_ALL);
    world.playSound(
        null,
        head,
        open ? SoundEvents.BLOCK_CHEST_OPEN : SoundEvents.BLOCK_CHEST_CLOSE,
        SoundCategory.BLOCKS,
        0.6F,
        LID_PITCH);
    world.emitGameEvent(player, open ? GameEvent.CONTAINER_OPEN : GameEvent.CONTAINER_CLOSE, head);
  }
}
