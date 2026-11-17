package com.grahambartley.nightborn.mirror;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class HandMirrorItem extends Item {

  public HandMirrorItem(final Settings settings) {
    super(settings);
  }

  @Override
  public TypedActionResult<ItemStack> use(
      final World world, final PlayerEntity user, final Hand hand) {
    final ItemStack stack = user.getStackInHand(hand);
    if (!Mirror.showsSelf(user.isSneaking())) {
      return TypedActionResult.pass(stack);
    }
    if (user instanceof ServerPlayerEntity player) {
      Mirror.raise(player, player);
    }
    return TypedActionResult.success(stack, world.isClient);
  }
}
