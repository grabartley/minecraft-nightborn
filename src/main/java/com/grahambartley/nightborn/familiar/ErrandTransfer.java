package com.grahambartley.nightborn.familiar;

import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public final class ErrandTransfer {

  private ErrandTransfer() {}

  public static int fetchOneStack(final Inventory chest, final Inventory carried) {
    for (int slot = 0; slot < chest.size(); slot++) {
      final ItemStack stack = chest.getStack(slot);
      if (stack.isEmpty()) {
        continue;
      }
      final int moved = moveInto(carried, stack);
      if (moved > 0) {
        chest.markDirty();
        return moved;
      }
    }
    return 0;
  }

  public static int deliverAll(final Inventory carried, final Inventory chest) {
    int delivered = 0;
    for (int slot = 0; slot < carried.size(); slot++) {
      final ItemStack stack = carried.getStack(slot);
      if (!stack.isEmpty()) {
        delivered += moveInto(chest, stack);
      }
    }
    if (delivered > 0) {
      carried.markDirty();
      chest.markDirty();
    }
    return delivered;
  }

  private static int moveInto(final Inventory to, final ItemStack stack) {
    final int before = stack.getCount();
    final ItemStack left = HopperBlockEntity.transfer(null, to, stack.copy(), null);
    final int moved = before - left.getCount();
    stack.decrement(moved);
    return moved;
  }
}
