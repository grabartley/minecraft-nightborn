package com.grahambartley.nightborn.servant.job;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;

public final class CarryRoom {

  private CarryRoom() {}

  public static boolean fits(final SimpleInventory carried, final List<ItemStack> drops) {
    final SimpleInventory trial = new SimpleInventory(carried.size());
    for (int slot = 0; slot < carried.size(); slot++) {
      trial.setStack(slot, carried.getStack(slot).copy());
    }
    final List<ItemStack> left = new ArrayList<>();
    for (final ItemStack drop : drops) {
      final ItemStack rest = trial.addStack(drop.copy());
      if (!rest.isEmpty()) {
        left.add(rest);
      }
    }
    return left.isEmpty();
  }
}
