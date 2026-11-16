package com.grahambartley.nightborn.occult;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public final class FamiliarOrders {

  public enum Order {
    NONE,
    NEXT_MODE,
    GIVE,
    TAKE_BACK
  }

  private FamiliarOrders() {}

  public static Order order(
      final boolean owner,
      final boolean mainHand,
      final boolean emptyHand,
      final boolean sneaking,
      final FamiliarMode mode) {
    if (!owner || !mainHand) {
      return Order.NONE;
    }
    if (emptyHand) {
      return sneaking ? Order.TAKE_BACK : Order.NEXT_MODE;
    }
    return mode == FamiliarMode.CARRY && !sneaking ? Order.GIVE : Order.NONE;
  }

  static ActionResult interact(
      final GhostlyFamiliarEntity familiar, final PlayerEntity player, final Hand hand) {
    final ItemStack held = player.getStackInHand(hand);
    final Order order =
        order(
            familiar.isOwnedBy(player),
            hand == Hand.MAIN_HAND,
            held.isEmpty(),
            player.isSneaking(),
            familiar.mode());
    if (order == Order.NONE) {
      return ActionResult.PASS;
    }
    if (familiar.getWorld().isClient) {
      return ActionResult.SUCCESS;
    }
    switch (order) {
      case NEXT_MODE -> {
        final FamiliarMode next = familiar.mode().next();
        familiar.order(next, player);
        tell(player, next.translationKey(), familiar);
      }
      case GIVE -> {
        final ItemStack left = familiar.carried().addStack(held.copy());
        if (left.getCount() == held.getCount()) {
          tell(player, "nightborn.familiar.full", familiar);
          return ActionResult.CONSUME;
        }
        player.setStackInHand(hand, left);
      }
      case TAKE_BACK -> {
        if (familiar.carried().isEmpty()) {
          return ActionResult.PASS;
        }
        familiar.returnCarried(player);
        tell(player, "nightborn.familiar.returned", familiar);
      }
      default -> {
        return ActionResult.PASS;
      }
    }
    familiar
        .getWorld()
        .playSound(
            null,
            familiar.getBlockPos(),
            SoundEvents.PARTICLE_SOUL_ESCAPE.value(),
            SoundCategory.NEUTRAL,
            0.6F,
            1.2F);
    return ActionResult.SUCCESS;
  }

  private static void tell(
      final PlayerEntity player, final String key, final GhostlyFamiliarEntity familiar) {
    player.sendMessage(Text.translatable(key, familiar.getDisplayName()), true);
  }
}
