package com.grahambartley.nightborn.hunt;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class HuntersBellItem extends Item {

  static final int RING_COOLDOWN_TICKS = 20;

  public HuntersBellItem(final Settings settings) {
    super(settings);
  }

  @Override
  public TypedActionResult<ItemStack> use(
      final World world, final PlayerEntity user, final Hand hand) {
    final ItemStack bell = user.getStackInHand(hand);
    user.getItemCooldownManager().set(this, RING_COOLDOWN_TICKS);
    if (user instanceof ServerPlayerEntity player) {
      BellRing.ring(player);
    }
    return TypedActionResult.success(bell, world.isClient());
  }
}
