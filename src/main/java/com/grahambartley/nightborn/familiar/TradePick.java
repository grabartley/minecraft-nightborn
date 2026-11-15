package com.grahambartley.nightborn.familiar;

import java.util.Optional;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;

public final class TradePick {

  private TradePick() {}

  public static Optional<TradeOffer> affordable(
      final TradeOfferList offers, final SimpleInventory carried) {
    for (final TradeOffer offer : offers) {
      if (canBuy(offer, carried)) {
        return Optional.of(offer);
      }
    }
    return Optional.empty();
  }

  public static boolean canBuy(final TradeOffer offer, final SimpleInventory carried) {
    if (offer.isDisabled()) {
      return false;
    }
    final int first = offer.getDisplayedFirstBuyItem().getCount();
    if (count(carried, offer.getFirstBuyItem()) < first) {
      return false;
    }
    final Optional<TradedItem> second = offer.getSecondBuyItem();
    if (second.isPresent() && count(carried, second.get()) < second.get().count()) {
      return false;
    }
    return carried.canInsert(offer.getSellItem());
  }

  public static ItemStack pay(final TradeOffer offer, final SimpleInventory carried) {
    remove(carried, offer.getFirstBuyItem(), offer.getDisplayedFirstBuyItem().getCount());
    offer.getSecondBuyItem().ifPresent(second -> remove(carried, second, second.count()));
    return carried.addStack(offer.copySellItem());
  }

  static int count(final SimpleInventory carried, final TradedItem wanted) {
    int total = 0;
    for (int slot = 0; slot < carried.size(); slot++) {
      final ItemStack stack = carried.getStack(slot);
      if (wanted.matches(stack)) {
        total += stack.getCount();
      }
    }
    return total;
  }

  private static void remove(
      final SimpleInventory carried, final TradedItem wanted, final int count) {
    int left = count;
    for (int slot = 0; slot < carried.size() && left > 0; slot++) {
      final ItemStack stack = carried.getStack(slot);
      if (wanted.matches(stack)) {
        final int taken = Math.min(left, stack.getCount());
        stack.decrement(taken);
        left -= taken;
      }
    }
    carried.markDirty();
  }
}
