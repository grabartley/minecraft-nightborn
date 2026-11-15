package com.grahambartley.nightborn.decor;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class WeaponRackBlock extends WallDecorBlock implements BlockEntityProvider {

  public static final MapCodec<WeaponRackBlock> CODEC = createCodec(WeaponRackBlock::new);
  private static final VoxelShape NORTH = Block.createCuboidShape(0.5, 1, 12, 15.5, 16, 16);

  public WeaponRackBlock(final Settings settings) {
    super(settings, NORTH);
  }

  @Override
  protected MapCodec<WeaponRackBlock> getCodec() {
    return CODEC;
  }

  @Override
  public BlockEntity createBlockEntity(final BlockPos pos, final BlockState state) {
    return new WeaponRackBlockEntity(pos, state);
  }

  private static Optional<RackUse> decide(
      final WeaponRackBlockEntity rack,
      final int hook,
      final ItemStack held,
      final PlayerEntity player) {
    return RackUse.decide(
        held.isIn(DecorTags.WEAPON_RACK_HOLDS),
        player.getMainHandStack().isEmpty(),
        rack.get(hook).isEmpty());
  }

  private static int hook(final BlockPos pos, final BlockHitResult hit) {
    return RackHooks.hookAt(hit.getPos().y - pos.getY());
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
    if (!(world.getBlockEntity(pos) instanceof WeaponRackBlockEntity rack)) {
      return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    final int hook = hook(pos, hit);
    if (!decide(rack, hook, stack, player).equals(Optional.of(RackUse.HANG))) {
      return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    if (!world.isClient) {
      rack.hang(hook, stack.splitUnlessCreative(1, player));
      world.playSound(
          null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 1.0f, 1.0f);
      world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
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
    if (!(world.getBlockEntity(pos) instanceof WeaponRackBlockEntity rack)) {
      return ActionResult.PASS;
    }
    final int hook = hook(pos, hit);
    if (!decide(rack, hook, player.getMainHandStack(), player).equals(Optional.of(RackUse.TAKE))) {
      return ActionResult.PASS;
    }
    if (!world.isClient) {
      player.setStackInHand(Hand.MAIN_HAND, rack.take(hook));
      world.playSound(
          null, pos, SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1.0f, 1.0f);
      world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
    }
    return ActionResult.success(world.isClient);
  }

  @Override
  protected void onStateReplaced(
      final BlockState state,
      final World world,
      final BlockPos pos,
      final BlockState newState,
      final boolean moved) {
    if (!state.isOf(newState.getBlock())
        && world.getBlockEntity(pos) instanceof WeaponRackBlockEntity rack) {
      rack.held()
          .forEach(stack -> ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), stack));
    }
    super.onStateReplaced(state, world, pos, newState, moved);
  }
}
