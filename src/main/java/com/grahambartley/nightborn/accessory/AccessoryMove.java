package com.grahambartley.nightborn.accessory;

import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import java.util.Optional;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public final class AccessoryMove {

  private AccessoryMove() {}

  public static void fromArmour(final PlayerEntity player) {
    for (final EquipmentSlot slot : EquipmentSlot.values()) {
      if (slot.isArmorSlot()) {
        move(player, slot);
      }
    }
  }

  static boolean misplaced(final boolean accessory, final boolean fitsArmourSlot) {
    return accessory && !fitsArmourSlot;
  }

  private static void move(final PlayerEntity player, final EquipmentSlot slot) {
    final ItemStack stack = player.getEquippedStack(slot);
    final Optional<AccessorySlot> home = AccessorySlot.fitting(stack);
    if (!misplaced(home.isPresent(), player.getPreferredEquipmentSlot(stack) == slot)) {
      return;
    }
    player.equipStack(slot, ItemStack.EMPTY);
    final Optional<TrinketInventory> inventory =
        TrinketsApi.getTrinketComponent(player).flatMap(component -> slotOf(component, home.get()));
    final int free = inventory.map(AccessoryMove::freeIndex).orElse(-1);
    switch (Relocation.of(free >= 0, player.getInventory().getEmptySlot() >= 0)) {
      case SLOT -> inventory.get().setStack(free, stack);
      case INVENTORY -> player.getInventory().insertStack(stack);
      case FEET -> player.dropItem(stack, false, true);
    }
  }

  private static Optional<TrinketInventory> slotOf(
      final TrinketComponent component, final AccessorySlot slot) {
    return Optional.ofNullable(component.getInventory().get(slot.group()))
        .map(group -> group.get(slot.slotName()));
  }

  private static int freeIndex(final TrinketInventory inventory) {
    for (int i = 0; i < inventory.size(); i++) {
      if (inventory.getStack(i).isEmpty()) {
        return i;
      }
    }
    return -1;
  }
}
