package com.grahambartley.nightborn.decor;

import com.grahambartley.nightborn.blood.BloodBottleItem;
import com.grahambartley.nightborn.blood.BloodItems;
import com.grahambartley.nightborn.blood.BloodQuality;
import com.grahambartley.nightborn.vampire.Vampires;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
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
import net.minecraft.world.event.GameEvent;

public class ChaliceBlock extends Block {

  public static final EnumProperty<ChaliceBlood> BLOOD =
      EnumProperty.of("blood", ChaliceBlood.class);
  public static final MapCodec<ChaliceBlock> CODEC = createCodec(ChaliceBlock::new);

  private static final VoxelShape SHAPE = Block.createCuboidShape(5, 0, 5, 11, 12, 11);

  public ChaliceBlock(final Settings settings) {
    super(settings);
    this.setDefaultState(this.getStateManager().getDefaultState().with(BLOOD, ChaliceBlood.EMPTY));
  }

  @Override
  protected MapCodec<ChaliceBlock> getCodec() {
    return CODEC;
  }

  @Override
  protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
    builder.add(BLOOD);
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

  public static ChalicePour.Held held(final ItemStack stack) {
    if (stack.isEmpty()) {
      return ChalicePour.Held.EMPTY_HAND;
    }
    if (stack.isOf(BloodItems.BLOOD_BOTTLE)) {
      return ChalicePour.Held.BLOOD_BOTTLE;
    }
    return stack.isOf(Items.GLASS_BOTTLE) ? ChalicePour.Held.GLASS_BOTTLE : ChalicePour.Held.OTHER;
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
    final ChaliceBlood contents = state.get(BLOOD);
    final ChalicePour pour =
        ChalicePour.decide(contents, held(stack), Vampires.isVampire(player)).orElse(null);
    if (pour == ChalicePour.FILL) {
      if (!world.isClient) {
        final BloodQuality quality = BloodBottleItem.qualityOf(stack);
        player.setStackInHand(
            hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
        this.change(
            world, pos, state, ChaliceBlood.of(quality), player, SoundEvents.ITEM_BOTTLE_EMPTY);
      }
      return ItemActionResult.success(world.isClient);
    }
    if (pour == ChalicePour.BOTTLE) {
      if (!world.isClient) {
        final BloodQuality quality = contents.quality().orElseThrow();
        player.setStackInHand(
            hand, ItemUsage.exchangeStack(stack, player, BloodBottleItem.of(quality)));
        this.change(world, pos, state, ChaliceBlood.EMPTY, player, SoundEvents.ITEM_BOTTLE_FILL);
      }
      return ItemActionResult.success(world.isClient);
    }
    return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
  }

  @Override
  protected ActionResult onUse(
      final BlockState state,
      final World world,
      final BlockPos pos,
      final PlayerEntity player,
      final BlockHitResult hit) {
    final ChaliceBlood contents = state.get(BLOOD);
    final boolean drinks =
        ChalicePour.decide(contents, held(player.getMainHandStack()), Vampires.isVampire(player))
            .filter(pour -> pour == ChalicePour.DRINK)
            .isPresent();
    if (!drinks) {
      return ActionResult.PASS;
    }
    if (!world.isClient) {
      BloodBottleItem.drink(player, contents.quality().orElseThrow());
      this.change(world, pos, state, ChaliceBlood.EMPTY, player, SoundEvents.ENTITY_GENERIC_DRINK);
    }
    return ActionResult.success(world.isClient);
  }

  private void change(
      final World world,
      final BlockPos pos,
      final BlockState state,
      final ChaliceBlood contents,
      final PlayerEntity player,
      final SoundEvent sound) {
    world.setBlockState(pos, state.with(BLOOD, contents), Block.NOTIFY_ALL);
    world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1.0f, 1.0f);
    world.emitGameEvent(
        player,
        contents == ChaliceBlood.EMPTY ? GameEvent.FLUID_PICKUP : GameEvent.FLUID_PLACE,
        pos);
  }
}
