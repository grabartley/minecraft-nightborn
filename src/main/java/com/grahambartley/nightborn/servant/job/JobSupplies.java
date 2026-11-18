package com.grahambartley.nightborn.servant.job;

import java.util.Comparator;
import java.util.function.Predicate;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;

public final class JobSupplies {

  private JobSupplies() {}

  public static int take(
      final Inventory from,
      final SimpleInventory into,
      final Predicate<ItemStack> wanted,
      final int most) {
    int moved = 0;
    for (int slot = 0; slot < from.size() && moved < most; slot++) {
      final ItemStack stack = from.getStack(slot);
      if (stack.isEmpty() || !wanted.test(stack)) {
        continue;
      }
      final ItemStack taken = stack.split(Math.min(stack.getCount(), most - moved));
      final int asked = taken.getCount();
      final ItemStack left = into.addStack(taken);
      moved += asked - left.getCount();
      if (!left.isEmpty()) {
        stack.increment(left.getCount());
        break;
      }
    }
    if (moved > 0) {
      from.markDirty();
    }
    return moved;
  }

  public static boolean takeBest(
      final Inventory from,
      final SimpleInventory into,
      final Predicate<ItemStack> wanted,
      final Comparator<ItemStack> better) {
    int best = -1;
    for (int slot = 0; slot < from.size(); slot++) {
      final ItemStack stack = from.getStack(slot);
      if (!stack.isEmpty()
          && wanted.test(stack)
          && (best < 0 || better.compare(stack, from.getStack(best)) > 0)) {
        best = slot;
      }
    }
    if (best < 0) {
      return false;
    }
    final ItemStack chosen = from.getStack(best);
    final ItemStack left = into.addStack(chosen.split(1));
    if (!left.isEmpty()) {
      chosen.increment(left.getCount());
      return false;
    }
    from.markDirty();
    return true;
  }

  public static boolean holds(final Inventory carried, final Predicate<ItemStack> wanted) {
    for (int slot = 0; slot < carried.size(); slot++) {
      final ItemStack stack = carried.getStack(slot);
      if (!stack.isEmpty() && wanted.test(stack)) {
        return true;
      }
    }
    return false;
  }

  public static ItemStack first(final Inventory carried, final Predicate<ItemStack> wanted) {
    for (int slot = 0; slot < carried.size(); slot++) {
      final ItemStack stack = carried.getStack(slot);
      if (!stack.isEmpty() && wanted.test(stack)) {
        return stack;
      }
    }
    return ItemStack.EMPTY;
  }
}
