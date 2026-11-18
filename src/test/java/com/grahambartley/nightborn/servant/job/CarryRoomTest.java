package com.grahambartley.nightborn.servant.job;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CarryRoomTest {

  @ParameterizedTest(name = "{0} full stacks carried, {1} wheat and {2} seeds -> fits {3}")
  @CsvSource({
    "0, 3, 2, true",
    "7, 64, 0, true",
    "7, 65, 0, false",
    "8, 1, 0, false",
    "6, 64, 64, true",
    "7, 1, 1, false",
    "8, 0, 0, true"
  })
  @DisplayName("a harvest is taken only if every drop fits, so nothing is lost on the ground")
  void fits(final int fullStacks, final int wheat, final int seeds, final boolean fits) {
    final SimpleInventory carried = new SimpleInventory(DayWorker.CARRY_SLOTS);
    for (int slot = 0; slot < fullStacks; slot++) {
      carried.setStack(slot, new ItemStack(Items.COBBLESTONE, 64));
    }
    final List<ItemStack> drops = new ArrayList<>();
    for (int left = wheat; left > 0; left -= Items.WHEAT.getMaxCount()) {
      drops.add(new ItemStack(Items.WHEAT, Math.min(left, Items.WHEAT.getMaxCount())));
    }
    if (seeds > 0) {
      drops.add(new ItemStack(Items.WHEAT_SEEDS, seeds));
    }
    final int before = carried.count(Items.COBBLESTONE);
    assertEquals(fits, CarryRoom.fits(carried, drops));
    assertEquals(before, carried.count(Items.COBBLESTONE));
    assertEquals(0, carried.count(Items.WHEAT));
  }
}
