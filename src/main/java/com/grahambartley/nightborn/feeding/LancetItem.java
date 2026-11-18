package com.grahambartley.nightborn.feeding;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class LancetItem extends Item {

  public static final int DURABILITY = 64;

  public LancetItem(final Settings settings) {
    super(settings);
  }

  @Override
  public TypedActionResult<ItemStack> use(
      final World world, final PlayerEntity user, final Hand hand) {
    final ActionResult result = Lancet.onSelf(user, hand);
    final ItemStack stack = user.getStackInHand(hand);
    return result == ActionResult.PASS
        ? TypedActionResult.pass(stack)
        : TypedActionResult.success(stack, world.isClient);
  }
}
