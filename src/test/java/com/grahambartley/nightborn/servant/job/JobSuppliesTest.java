package com.grahambartley.nightborn.servant.job;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class JobSuppliesTest {

  @ParameterizedTest(name = "{0} bottles in the chest, {1} carried slots full, take {2} -> {3}")
  @CsvSource({
    "10, 0, 64, 10",
    "10, 0, 4, 4",
    "0, 0, 64, 0",
    "80, 0, 64, 64",
    "10, 8, 64, 0",
    "10, 7, 64, 10"
  })
  @DisplayName(
      "a servant takes only what it was sent for, as much as it can carry, and nothing vanishes")
  void take(final int inChest, final int fullSlots, final int most, final int moved) {
    final SimpleInventory chest = new SimpleInventory(27);
    chest.setStack(0, new ItemStack(Items.WHEAT, 5));
    if (inChest > 0) {
      chest.setStack(1, new ItemStack(Items.GLASS_BOTTLE, Math.min(inChest, 64)));
    }
    if (inChest > 64) {
      chest.setStack(2, new ItemStack(Items.GLASS_BOTTLE, inChest - 64));
    }
    final SimpleInventory carried = new SimpleInventory(DayWorker.CARRY_SLOTS);
    for (int slot = 0; slot < fullSlots; slot++) {
      carried.setStack(slot, new ItemStack(Items.COBBLESTONE, 64));
    }
    assertEquals(
        moved, JobSupplies.take(chest, carried, stack -> stack.isOf(Items.GLASS_BOTTLE), most));
    assertEquals(moved, carried.count(Items.GLASS_BOTTLE));
    assertEquals(inChest - moved, chest.count(Items.GLASS_BOTTLE));
    assertEquals(5, chest.count(Items.WHEAT));
  }

  @ParameterizedTest(name = "carrying a lancet {0}")
  @CsvSource({"true", "false"})
  @DisplayName("a servant knows whether it holds a tool and which stack it is")
  void holdsAndFirst(final boolean carrying) {
    final SimpleInventory carried = new SimpleInventory(DayWorker.CARRY_SLOTS);
    carried.setStack(3, new ItemStack(carrying ? Items.IRON_PICKAXE : Items.WHEAT));
    assertEquals(carrying, JobSupplies.holds(carried, stack -> stack.isOf(Items.IRON_PICKAXE)));
    assertEquals(
        carrying,
        JobSupplies.first(carried, stack -> stack.isOf(Items.IRON_PICKAXE)) == carried.getStack(3));
  }

  @ParameterizedTest(name = "{0} then {1} -> takes {2}")
  @CsvSource({
    "WOODEN_PICKAXE, IRON_PICKAXE, IRON_PICKAXE",
    "DIAMOND_PICKAXE, STONE_PICKAXE, DIAMOND_PICKAXE",
    "WHEAT, STONE_PICKAXE, STONE_PICKAXE"
  })
  @DisplayName("a servant takes the best tool in the chest, one of it, and leaves the rest")
  void takeBest(final String first, final String second, final String taken) {
    final SimpleInventory chest = new SimpleInventory(27);
    chest.setStack(0, new ItemStack(item(first)));
    chest.setStack(1, new ItemStack(item(second)));
    final SimpleInventory carried = new SimpleInventory(DayWorker.CARRY_SLOTS);
    assertEquals(
        true,
        JobSupplies.takeBest(
            chest,
            carried,
            stack -> stack.getItem() instanceof net.minecraft.item.PickaxeItem,
            PickaxeRank.BEST_FIRST));
    assertEquals(1, carried.count(item(taken)));
    assertEquals(1, chest.count(item(first)) + chest.count(item(second)));
  }

  private static net.minecraft.item.Item item(final String name) {
    return net.minecraft.registry.Registries.ITEM.get(
        net.minecraft.util.Identifier.ofVanilla(name.toLowerCase(java.util.Locale.ROOT)));
  }
}
