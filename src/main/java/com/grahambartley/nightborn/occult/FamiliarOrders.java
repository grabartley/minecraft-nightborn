package com.grahambartley.nightborn.occult;

import com.grahambartley.nightborn.wheel.CommandWheel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public final class FamiliarOrders {

  public enum Order {
    WHEEL,
    GIVE,
    TAKE_BACK
  }

  private FamiliarOrders() {}

  public static Order order(
      final boolean owner,
      final boolean mainHand,
      final boolean emptyHand,
      final boolean sneaking,
      final boolean carrying,
      final FamiliarMode mode) {
    if (!owner || !mainHand) {
      return Order.WHEEL;
    }
    if (emptyHand) {
      return sneaking && carrying ? Order.TAKE_BACK : Order.WHEEL;
    }
    return mode == FamiliarMode.CARRY && !sneaking ? Order.GIVE : Order.WHEEL;
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
            !familiar.carried().isEmpty(),
            familiar.mode());
    if (order == Order.WHEEL) {
      return CommandWheel.use(player, hand, familiar);
    }
    if (familiar.getWorld().isClient) {
      return ActionResult.SUCCESS;
    }
    if (order == Order.GIVE) {
      final ItemStack left = familiar.carried().addStack(held.copy());
      if (left.getCount() == held.getCount()) {
        tell(player, "nightborn.familiar.full", familiar);
        return ActionResult.CONSUME;
      }
      player.setStackInHand(hand, left);
    } else {
      familiar.returnCarried(player);
      tell(player, "nightborn.familiar.returned", familiar);
    }
    FamiliarModeOrders.whisper(familiar);
    return ActionResult.SUCCESS;
  }

  private static void tell(
      final PlayerEntity player, final String key, final GhostlyFamiliarEntity familiar) {
    player.sendMessage(Text.translatable(key, familiar.getDisplayName()), true);
  }
}
