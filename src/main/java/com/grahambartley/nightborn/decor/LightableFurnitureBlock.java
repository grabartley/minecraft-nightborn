package com.grahambartley.nightborn.decor;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;

public abstract class LightableFurnitureBlock extends BlockWithEntity implements AnimatedFurniture {

  public static final BooleanProperty LIT = Properties.LIT;
  public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
  private static final float AMBIENT_CHANCE = 0.08f;

  protected LightableFurnitureBlock(final Settings settings) {
    super(settings);
    this.setDefaultState(
        this.getStateManager().getDefaultState().with(LIT, false).with(FACING, Direction.NORTH));
  }

  protected abstract VoxelShape shapeFor(BlockState state);

  protected abstract List<Vec3d> wicks();

  protected abstract Direction supportSide();

  @Override
  public BooleanProperty toggleProperty() {
    return LIT;
  }

  @Override
  public boolean glows(final BlockState state) {
    return state.get(LIT);
  }

  @Override
  protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
    builder.add(LIT, FACING);
  }

  @Override
  public BlockState getPlacementState(final ItemPlacementContext context) {
    final BlockState state =
        this.getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite());
    return state.canPlaceAt(context.getWorld(), context.getBlockPos()) ? state : null;
  }

  @Override
  protected boolean canPlaceAt(final BlockState state, final WorldView world, final BlockPos pos) {
    final Direction side = this.supportSide();
    return Block.sideCoversSmallSquare(world, pos.offset(side), side.getOpposite());
  }

  @Override
  protected BlockState getStateForNeighborUpdate(
      final BlockState state,
      final Direction direction,
      final BlockState neighborState,
      final WorldAccess world,
      final BlockPos pos,
      final BlockPos neighborPos) {
    if (direction == this.supportSide() && !state.canPlaceAt(world, pos)) {
      return Blocks.AIR.getDefaultState();
    }
    return super.getStateForNeighborUpdate(
        state, direction, neighborState, world, pos, neighborPos);
  }

  @Override
  protected VoxelShape getOutlineShape(
      final BlockState state,
      final BlockView world,
      final BlockPos pos,
      final ShapeContext context) {
    return this.shapeFor(state);
  }

  @Override
  protected BlockRenderType getRenderType(final BlockState state) {
    return BlockRenderType.ENTITYBLOCK_ANIMATED;
  }

  @Override
  public BlockEntity createBlockEntity(final BlockPos pos, final BlockState state) {
    return new FurnitureBlockEntity(pos, state);
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
    final FlameToggle toggle = FlameToggle.decide(state.get(LIT), stack);
    if (!toggle.lights()) {
      return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    if (!world.isClient) {
      if (toggle == FlameToggle.LIGHT_WITH_FLINT) {
        stack.damage(1, player, LivingEntity.getSlotForHand(hand));
      } else {
        stack.decrementUnlessCreative(1, player);
      }
      this.setLit(
          world,
          pos,
          state,
          true,
          player,
          toggle == FlameToggle.LIGHT_WITH_FLINT
              ? SoundEvents.ITEM_FLINTANDSTEEL_USE
              : SoundEvents.ITEM_FIRECHARGE_USE);
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
    if (FlameToggle.decide(state.get(LIT), player.getMainHandStack()) != FlameToggle.DOUSE) {
      return ActionResult.PASS;
    }
    if (!world.isClient) {
      this.setLit(world, pos, state, false, player, SoundEvents.BLOCK_CANDLE_EXTINGUISH);
      this.puffSmoke((ServerWorld) world, pos, state);
    }
    return ActionResult.success(world.isClient);
  }

  @Override
  public void randomDisplayTick(
      final BlockState state, final World world, final BlockPos pos, final Random random) {
    if (state.get(LIT) && random.nextFloat() < AMBIENT_CHANCE) {
      world.playSound(
          pos.getX() + 0.5,
          pos.getY() + 0.5,
          pos.getZ() + 0.5,
          SoundEvents.BLOCK_CANDLE_AMBIENT,
          SoundCategory.BLOCKS,
          1.0f + random.nextFloat(),
          random.nextFloat() * 0.7f + 0.3f,
          false);
    }
  }

  private void setLit(
      final World world,
      final BlockPos pos,
      final BlockState state,
      final boolean lit,
      final PlayerEntity player,
      final SoundEvent sound) {
    world.setBlockState(pos, state.with(LIT, lit), Block.NOTIFY_ALL_AND_REDRAW);
    world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1.0f, 1.0f);
    world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
  }

  private void puffSmoke(final ServerWorld world, final BlockPos pos, final BlockState state) {
    for (final Vec3d wick : this.wicks()) {
      final Vec3d at = FacingShapes.turn(wick, state.get(FACING)).add(Vec3d.of(pos));
      world.spawnParticles(ParticleTypes.SMOKE, at.x, at.y, at.z, 2, 0.01, 0.02, 0.01, 0.01);
    }
  }
}
