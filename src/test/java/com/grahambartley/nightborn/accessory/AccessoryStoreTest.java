package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.blood.BloodItems;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class AccessoryStoreTest {

  private static ItemStack pendant() {
    return new ItemStack(AccessoryItems.of("blood_vial_pendant"));
  }

  @ParameterizedTest(name = "room {0}, offered {1}: takes {2}, leaves {3}")
  @CsvSource({"1, 1, 1, 0", "1, 3, 1, 2", "2, 3, 2, 1"})
  @DisplayName("a store takes what fits and leaves the rest where it was, so nothing is lost")
  void putConserves(final int count, final int offered, final int taken, final int left) {
    final ItemStack store = pendant();
    final ItemStack bottles = new ItemStack(BloodItems.BLOOD_BOTTLE, offered);
    assertEquals(
        taken,
        AccessoryStore.put(
            new AccessoryHold(List.of(BloodItems.BLOOD_BOTTLE), count), store, bottles));
    assertEquals(left, bottles.getCount());
    assertEquals(taken, AccessoryStore.held(store));
    assertEquals(offered, AccessoryStore.held(store) + bottles.getCount());
  }

  @Test
  @DisplayName("a store refuses what its data does not name")
  void refuses() {
    final ItemStack store = pendant();
    final ItemStack glass = new ItemStack(Items.GLASS_BOTTLE);
    assertEquals(
        0,
        AccessoryStore.put(new AccessoryHold(List.of(BloodItems.BLOOD_BOTTLE), 1), store, glass));
    assertEquals(1, glass.getCount());
    assertTrue(AccessoryStore.contents(store).isEmpty());
  }

  @Test
  @DisplayName("what goes in comes back out, and the store is then empty")
  void takeBack() {
    final ItemStack store = pendant();
    AccessoryStore.put(
        new AccessoryHold(List.of(BloodItems.BLOOD_BOTTLE), 1),
        store,
        new ItemStack(BloodItems.BLOOD_BOTTLE));
    assertTrue(AccessoryStore.take(store).orElseThrow().isOf(BloodItems.BLOOD_BOTTLE));
    assertEquals(0, AccessoryStore.held(store));
    assertTrue(AccessoryStore.take(store).isEmpty());
  }
}
