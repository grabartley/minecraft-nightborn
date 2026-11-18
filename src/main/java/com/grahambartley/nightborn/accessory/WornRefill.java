package com.grahambartley.nightborn.accessory;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.util.Hand;

public final class WornRefill {

  private WornRefill() {}

  public static void broke(final PlayerEntity player, final Item item, final EquipmentSlot slot) {
    final MinecraftServer server = player.getServer();
    final Optional<Hand> hand = hand(slot);
    if (server == null || hand.isEmpty()) {
      return;
    }
    final ItemStack usedUp = new ItemStack(item);
    server.send(
        new ServerTask(
            server.getTicks(),
            () -> {
              if (!player.isRemoved() && player.getStackInHand(hand.get()).isEmpty()) {
                next(player, usedUp).ifPresent(stack -> player.setStackInHand(hand.get(), stack));
              }
            }));
  }

  public static Optional<ItemStack> next(final PlayerEntity player, final ItemStack usedUp) {
    if (player.getWorld().isClient() || usedUp.isEmpty()) {
      return Optional.empty();
    }
    for (final WornStores.WornStore worn : WornStores.worn(player)) {
      final Optional<ItemStack> next =
          AccessoryHold.fitting(worn.holds(), usedUp)
              .flatMap(hold -> AccessoryStore.takeOne(worn.stack(), wanted(worn, hold, usedUp)));
      if (next.isPresent()) {
        AccessoryStore.soundTake(player);
        return next;
      }
    }
    return Optional.empty();
  }

  static Optional<Hand> hand(final EquipmentSlot slot) {
    return switch (slot) {
      case MAINHAND -> Optional.of(Hand.MAIN_HAND);
      case OFFHAND -> Optional.of(Hand.OFF_HAND);
      default -> Optional.empty();
    };
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
