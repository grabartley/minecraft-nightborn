package com.grahambartley.nightborn.familiar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ErrandTransferTest {

  @ParameterizedTest(name = "{0} wheat in slot {1}, {2} carried: moves {3}")
  @CsvSource({"64, 0, 0, 64", "20, 5, 0, 20", "64, 0, 60, 64", "0, 0, 0, 0"})
  @DisplayName("a fetch moves the first stack and conserves every item")
  void fetch(final int count, final int slot, final int carriedCount, final int moved) {
    final SimpleInventory chest = new SimpleInventory(27);
    chest.setStack(slot, new ItemStack(Items.WHEAT, count));
    chest.setStack(10, new ItemStack(Items.CARROT, 3));
    final SimpleInventory carried = new SimpleInventory(HumanFamiliarEntity.CARRY_SLOTS);
    carried.setStack(0, new ItemStack(Items.WHEAT, carriedCount));
    final int before = total(chest, Items.WHEAT) + total(carried, Items.WHEAT);
    final int expected = count == 0 ? 3 : moved;
    assertEquals(expected, ErrandTransfer.fetchOneStack(chest, carried));
    assertEquals(before, total(chest, Items.WHEAT) + total(carried, Items.WHEAT));
  }

  @ParameterizedTest(name = "{0} free chest slots: delivers {1}")
  @CsvSource({"27, 96", "1, 64", "0, 0"})
  @DisplayName("a delivery moves what fits and keeps the rest carried")
  void deliver(final int freeSlots, final int delivered) {
    final SimpleInventory chest = new SimpleInventory(27);
    for (int slot = freeSlots; slot < chest.size(); slot++) {
      chest.setStack(slot, new ItemStack(Items.STONE, 64));
    }
    final SimpleInventory carried = new SimpleInventory(HumanFamiliarEntity.CARRY_SLOTS);
    carried.setStack(0, new ItemStack(Items.BREAD, 64));
    carried.setStack(3, new ItemStack(Items.BREAD, 32));
    assertEquals(delivered, ErrandTransfer.deliverAll(carried, chest));
    assertEquals(96, total(chest, Items.BREAD) + total(carried, Items.BREAD));
  }

  private static int total(final Inventory inventory, final Item item) {
    return inventory.count(item);
  }
}
