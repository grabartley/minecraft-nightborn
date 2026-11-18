package com.grahambartley.nightborn.accessory;

import dev.emi.trinkets.api.TrinketsApi;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class AccessoryOwnership {

  private AccessoryOwnership() {}

  public static Set<Item> items(final PlayerEntity player) {
    final List<ItemStack> stacks = new ArrayList<>();
    stacks.addAll(player.getInventory().main);
    stacks.addAll(player.getInventory().armor);
    stacks.addAll(player.getInventory().offHand);
    stacks.addAll(player.getEnderChestInventory().getHeldStacks());
    TrinketsApi.getTrinketComponent(player)
        .ifPresent(component -> component.forEach((ref, stack) -> stacks.add(stack)));
    return collect(stacks);
  }

  public static Set<AccessorySlot> worn(final PlayerEntity player) {
    final Set<AccessorySlot> slots = EnumSet.noneOf(AccessorySlot.class);
    TrinketsApi.getTrinketComponent(player)
        .ifPresent(
            component ->
                component.forEach(
                    (ref, stack) -> {
                      if (!stack.isEmpty()) {
                        final var type = ref.inventory().getSlotType();
                        AccessorySlot.byId(type.getGroup() + "/" + type.getName())
                            .ifPresent(slots::add);
                      }
                    }));
    return slots;
  }

  public static Set<Item> collect(final Iterable<ItemStack> stacks) {
    final Set<Item> items = new HashSet<>();
    for (final ItemStack stack : stacks) {
      if (!stack.isEmpty()) {
        items.add(stack.getItem());
      }
    }
    return items;
  }
}
