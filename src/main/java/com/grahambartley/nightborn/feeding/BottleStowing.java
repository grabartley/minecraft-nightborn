package com.grahambartley.nightborn.feeding;

import java.util.function.IntPredicate;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

final class BottleStowing {

  private BottleStowing() {}

  static boolean stow(final PlayerInventory inventory, final ItemStack bottle) {
    final int slot = slotFor(inventory, bottle);
    if (slot < 0) {
      return false;
    }
    final ItemStack held = inventory.main.get(slot);
    if (held.isEmpty()) {
      inventory.main.set(slot, bottle.copy());
    } else {
      held.increment(bottle.getCount());
    }
    bottle.setCount(0);
    inventory.markDirty();
    return true;
  }

  static int slotFor(final PlayerInventory inventory, final ItemStack bottle) {
    return slotFor(
        inventory.main.size(),
        inventory.selectedSlot,
        slot -> inventory.main.get(slot).isEmpty(),
        slot -> {
          final ItemStack held = inventory.main.get(slot);
          return ItemStack.areItemsAndComponentsEqual(held, bottle)
              && held.getCount() + bottle.getCount() <= held.getMaxCount();
        });
  }

  static int slotFor(
      final int size, final int selected, final IntPredicate empty, final IntPredicate joins) {
    int firstEmpty = -1;
    for (int slot = 0; slot < size; slot++) {
      if (slot == selected) {
        continue;
      }
      if (empty.test(slot)) {
        firstEmpty = firstEmpty < 0 ? slot : firstEmpty;
      } else if (joins.test(slot)) {
        return slot;
      }
    }
    return firstEmpty;
  }
}
