package com.grahambartley.nightborn.decor;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class ChaliceBlock extends Block {

  public static final EnumProperty<ChaliceBlood> BLOOD =
      EnumProperty.of("blood", ChaliceBlood.class);
  public static final IntProperty BOTTLES = IntProperty.of("bottles", 1, ChaliceContents.CAPACITY);
  public static final MapCodec<ChaliceBlock> CODEC = createCodec(ChaliceBlock::new);

  private static final VoxelShape SHAPE = Block.createCuboidShape(5, 0, 5, 11, 12, 11);

  public ChaliceBlock(final Settings settings) {
    super(settings);
    this.setDefaultState(
        this.getStateManager().getDefaultState().with(BLOOD, ChaliceBlood.EMPTY).with(BOTTLES, 1));
  }

  @Override
  protected MapCodec<ChaliceBlock> getCodec() {
    return CODEC;
  }

  @Override
  protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
    builder.add(BLOOD, BOTTLES);
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
  protected boolean canPlaceAt(final BlockState state, final WorldView world, final BlockPos pos) {
    return Block.sideCoversSmallSquare(world, pos.down(), Direction.UP);
  }

  @Override
  protected BlockState getStateForNeighborUpdate(
      final BlockState state,
      final Direction direction,
      final BlockState neighborState,
      final WorldAccess world,
      final BlockPos pos,
      final BlockPos neighborPos) {
    return direction == Direction.DOWN && !state.canPlaceAt(world, pos)
        ? Blocks.AIR.getDefaultState()
        : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
  }

  public static ChaliceContents contents(final BlockState state) {
    return new ChaliceContents(state.get(BLOOD), state.get(BOTTLES));
  }

  public static ChaliceContents contents(final ItemStack chalice) {
    return contents(
        chalice
            .getOrDefault(DataComponentTypes.BLOCK_STATE, BlockStateComponent.DEFAULT)
            .applyToState(DecorBlocks.CHALICE.getDefaultState()));
  }

  public static ItemStack item(final ChaliceContents contents) {
    final ItemStack chalice = new ItemStack(DecorBlocks.CHALICE);
    if (!contents.empty()) {
      chalice.set(
          DataComponentTypes.BLOCK_STATE,
          BlockStateComponent.DEFAULT
              .with(BLOOD, contents.blood())
              .with(BOTTLES, contents.stateBottles()));
    }
    return chalice;
  }

  public static BlockState with(final BlockState state, final ChaliceContents contents) {
    return state.with(BLOOD, contents.blood()).with(BOTTLES, contents.stateBottles());
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
    final Optional<ChalicePour> pour = ChaliceUse.decide(contents(state), stack, player);
    if (pour.isEmpty() || pour.get() == ChalicePour.DRINK) {
      return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    if (!world.isClient) {
      store(
          world,
          pos,
          state,
          ChaliceUse.apply(pour.get(), contents(state), world, pos, player, hand));
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
    final boolean drinks =
        ChaliceUse.decide(contents(state), player.getMainHandStack(), player)
            .filter(pour -> pour == ChalicePour.DRINK)
            .isPresent();
    if (!drinks) {
      return ActionResult.PASS;
    }
    if (!world.isClient) {
      store(
          world,
          pos,
          state,
          ChaliceUse.apply(ChalicePour.DRINK, contents(state), world, pos, player, Hand.MAIN_HAND));
    }
    return ActionResult.success(world.isClient);
  }

  private static void store(
      final World world, final BlockPos pos, final BlockState state, final ChaliceContents next) {
    final BlockState changed = with(state, next);
    if (changed != state) {
      world.setBlockState(pos, changed, Block.NOTIFY_ALL);
    }
  }
}
