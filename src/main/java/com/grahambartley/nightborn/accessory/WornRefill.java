package com.grahambartley.nightborn.accessory;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public final class WornRefill {

  private WornRefill() {}

  public static void broke(final PlayerEntity player, final Item item, final EquipmentSlot slot) {
    if (slot == EquipmentSlot.MAINHAND) {
      refill(player, Hand.MAIN_HAND, new ItemStack(item));
    } else if (slot == EquipmentSlot.OFFHAND) {
      refill(player, Hand.OFF_HAND, new ItemStack(item));
    }
  }

  public static void refill(final PlayerEntity player, final Hand hand, final ItemStack usedUp) {
    if (player.getWorld().isClient()
        || usedUp.isEmpty()
        || !player.getStackInHand(hand).isEmpty()) {
      return;
    }
    for (final WornStores.WornStore worn : WornStores.worn(player)) {
      final Optional<ItemStack> next =
          AccessoryHold.fitting(worn.holds(), usedUp)
              .flatMap(hold -> AccessoryStore.takeOne(worn.stack(), wanted(worn, hold, usedUp)));
      if (next.isPresent()) {
        player.setStackInHand(hand, next.get());
        AccessoryStore.soundTake(player);
        return;
      }
    }
  }

  static Predicate<ItemStack> wanted(
      final WornStores.WornStore worn, final AccessoryHold hold, final ItemStack usedUp) {
    final List<ItemStack> held = AccessoryStore.contents(worn.stack()).stream().toList();
    return like(held, usedUp)
        ? stack -> ItemStack.areItemsAndComponentsEqual(stack, usedUp)
        : hold::accepts;
  }

  static boolean like(final List<ItemStack> held, final ItemStack usedUp) {
    return held.stream().anyMatch(stack -> ItemStack.areItemsAndComponentsEqual(stack, usedUp));
  }
}
