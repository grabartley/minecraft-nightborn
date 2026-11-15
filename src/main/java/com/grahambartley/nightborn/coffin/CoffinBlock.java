package com.grahambartley.nightborn.coffin;

import com.grahambartley.nightborn.vampire.Vampires;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;

public class CoffinBlock extends BedBlock {

  public static final MapCodec<BedBlock> CODEC = createCodec(CoffinBlock::new);
  public static final BooleanProperty OPEN = Properties.OPEN;
  public static final BooleanProperty STANDING = BooleanProperty.of("standing");

  static final float LID_VOLUME = 0.6F;
  static final float LID_PITCH = 0.55F;

  public CoffinBlock(final Settings settings) {
    super(DyeColor.BLACK, settings);
    this.setDefaultState(this.getDefaultState().with(OPEN, false).with(STANDING, false));
  }

  @Override
  public MapCodec<BedBlock> getCodec() {
    return CODEC;
  }

  @Override
  protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
    super.appendProperties(builder);
    builder.add(OPEN, STANDING);
  }

  @Override
  protected VoxelShape getOutlineShape(
      final BlockState state,
      final BlockView world,
      final BlockPos pos,
      final ShapeContext context) {
    return CoffinShape.of(state.get(PART), state.get(FACING), state.get(STANDING));
  }

  @Override
  public BlockState getPlacementState(final ItemPlacementContext ctx) {
    final boolean standing = ctx.getPlayer() != null && ctx.getPlayer().isSneaking();
    final Direction facing = ctx.getHorizontalPlayerFacing();
    final BlockPos head =
        ctx.getBlockPos().offset(CoffinLayout.towardsOtherPart(BedPart.FOOT, facing, standing));
    final World world = ctx.getWorld();
    return world.getBlockState(head).canReplace(ctx)
            && world.getWorldBorder().contains(head)
            && world.isInBuildLimit(head)
        ? this.getDefaultState().with(FACING, facing).with(STANDING, standing)
        : null;
  }

  @Override
  public void onPlaced(
      final World world,
      final BlockPos pos,
      final BlockState state,
      final LivingEntity placer,
      final ItemStack itemStack) {
    if (world.isClient) {
      return;
    }
    world.setBlockState(head(pos, state), state.with(PART, BedPart.HEAD), Block.NOTIFY_ALL);
    world.updateNeighbors(pos, Blocks.AIR);
    state.updateNeighbors(world, pos, Block.NOTIFY_ALL);
  }

  @Override
  public BlockState onBreak(
      final World world, final BlockPos pos, final BlockState state, final PlayerEntity player) {
    if (!world.isClient && player.isCreative() && state.get(PART) == BedPart.FOOT) {
      final BlockPos head = head(pos, state);
      final BlockState headState = world.getBlockState(head);
      if (headState.isOf(this) && headState.get(PART) == BedPart.HEAD) {
        world.setBlockState(
            head, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.SKIP_DROPS);
        world.syncWorldEvent(
            player, WorldEvents.BLOCK_BROKEN, head, Block.getRawIdFromState(headState));
      }
    }
    this.spawnBreakParticles(world, player, pos, state);
    world.emitGameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Emitter.of(player, state));
    return state;
  }

  @Override
  protected void onStateReplaced(
      final BlockState state,
      final World world,
      final BlockPos pos,
      final BlockState newState,
      final boolean moved) {
    if (world instanceof ServerWorld server
        && state.get(PART) == BedPart.HEAD
        && !newState.isOf(this)) {
      CoffinOwnership.releaseAt(server, pos);
    }
    super.onStateReplaced(state, world, pos, newState, moved);
  }

  @Override
  protected long getRenderingSeed(final BlockState state, final BlockPos pos) {
    final BlockPos foot =
        CoffinLayout.foot(pos, state.get(PART), state.get(FACING), state.get(STANDING));
    return MathHelper.hashCode(foot.getX(), foot.getY(), foot.getZ());
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
      return ActionResult.SUCCESS;
    }
    final BlockPos head = head(pos, state);
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
    if (direction
        != CoffinLayout.towardsOtherPart(state.get(PART), state.get(FACING), state.get(STANDING))) {
      return state;
    }
    return CoffinBlock.pairs(state, neighborState)
        ? state.with(OCCUPIED, neighborState.get(OCCUPIED)).with(OPEN, neighborState.get(OPEN))
        : Blocks.AIR.getDefaultState();
  }

  public static void openLid(final World world, final BlockPos head) {
    final BlockState state = world.getBlockState(head);
    if (state.getBlock() instanceof CoffinBlock coffin && !state.get(OPEN)) {
      coffin.setLid(world, head, state, true, null);
    }
  }

  public static boolean isStanding(final BlockState state) {
    return state.getBlock() instanceof CoffinBlock && state.get(STANDING);
  }

  static boolean pairs(final BlockState state, final BlockState neighborState) {
    return neighborState.isOf(state.getBlock())
        && neighborState.get(PART) != state.get(PART)
        && neighborState.get(FACING) == state.get(FACING)
        && neighborState.get(STANDING) == state.get(STANDING);
  }

  private static BlockPos head(final BlockPos pos, final BlockState state) {
    return CoffinLayout.head(pos, state.get(PART), state.get(FACING), state.get(STANDING));
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
        LID_VOLUME,
        LID_PITCH);
    world.emitGameEvent(player, open ? GameEvent.CONTAINER_OPEN : GameEvent.CONTAINER_CLOSE, head);
  }
}
