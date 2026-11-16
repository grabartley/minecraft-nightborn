package com.grahambartley.nightborn.decor;

import java.util.List;
import java.util.function.ToIntFunction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
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

public abstract class LightableFurnitureBlock extends FurnitureBlock {

  public static final BooleanProperty LIT = Properties.LIT;
  private static final float AMBIENT_CHANCE = 0.08f;
  private static final float SPARK_CHANCE = 0.2f;
  private static final float WISP_CHANCE = 0.08f;
  private static final double FLAME_TIP = 2.0 / 16;

  protected LightableFurnitureBlock(final Settings settings) {
    super(settings);
    this.setDefaultState(this.getDefaultState().with(LIT, false));
  }

  public static ToIntFunction<BlockState> luminance(final int level) {
    return state -> state.get(LIT) ? level : 0;
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
    super.appendProperties(builder);
    builder.add(LIT);
  }

  @Override
  public BlockState getPlacementState(final ItemPlacementContext context) {
    final BlockState state = this.facingPlacer(context);
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
      if (toggle.cost() == FlameToggle.Cost.WEAR) {
        stack.damage(1, player, LivingEntity.getSlotForHand(hand));
      } else {
        stack.decrementUnlessCreative(1, player);
      }
      this.apply(world, pos, state, player, toggle);
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
    final FlameToggle toggle = FlameToggle.decide(state.get(LIT), player.getMainHandStack());
    if (toggle != FlameToggle.DOUSE) {
      return ActionResult.PASS;
    }
    if (!world.isClient) {
      this.apply(world, pos, state, player, toggle);
    }
    return ActionResult.success(world.isClient);
  }

  @Override
  public void randomDisplayTick(
      final BlockState state, final World world, final BlockPos pos, final Random random) {
    if (!state.get(LIT)) {
      return;
    }
    for (final Vec3d wick : this.wicks()) {
      final Vec3d at = FacingShapes.atWick(pos, wick, state.get(FACING));
      if (random.nextFloat() < SPARK_CHANCE) {
        world.addParticle(ParticleTypes.SMALL_FLAME, at.x, at.y + FLAME_TIP, at.z, 0.0, 0.0, 0.0);
      }
      if (random.nextFloat() < WISP_CHANCE) {
        world.addParticle(ParticleTypes.SMOKE, at.x, at.y + FLAME_TIP, at.z, 0.0, 0.0, 0.0);
      }
    }
    if (random.nextFloat() < AMBIENT_CHANCE) {
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

  public void apply(
      final World world,
      final BlockPos pos,
      final BlockState state,
      final Entity actor,
      final FlameToggle toggle) {
    world.setBlockState(pos, state.with(LIT, toggle.lights()), Block.NOTIFY_ALL_AND_REDRAW);
    toggle
        .sound()
        .ifPresent(sound -> world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1.0f, 1.0f));
    if (toggle == FlameToggle.DOUSE && world instanceof ServerWorld server) {
      for (final Vec3d wick : this.wicks()) {
        final Vec3d at = FacingShapes.atWick(pos, wick, state.get(FACING));
        server.spawnParticles(ParticleTypes.SMOKE, at.x, at.y, at.z, 2, 0.01, 0.02, 0.01, 0.01);
      }
    }
    world.emitGameEvent(actor, GameEvent.BLOCK_CHANGE, pos);
  }
}
