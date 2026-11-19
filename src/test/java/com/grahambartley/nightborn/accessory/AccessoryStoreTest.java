package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.ammo.AmmoItems;
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
            List.of(new AccessoryHold(List.of(BloodItems.BLOOD_BOTTLE), count)), store, bottles));
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
        AccessoryStore.put(
            List.of(new AccessoryHold(List.of(BloodItems.BLOOD_BOTTLE), 1)), store, glass));
    assertEquals(1, glass.getCount());
    assertTrue(AccessoryStore.contents(store).isEmpty());
  }

  @Test
  @DisplayName("what goes in comes back out, and the store is then empty")
  void takeBack() {
    final ItemStack store = pendant();
    AccessoryStore.put(
        List.of(new AccessoryHold(List.of(BloodItems.BLOOD_BOTTLE), 1)),
        store,
        new ItemStack(BloodItems.BLOOD_BOTTLE));
    assertTrue(AccessoryStore.take(store).orElseThrow().isOf(BloodItems.BLOOD_BOTTLE));
    assertEquals(0, AccessoryStore.held(store));
    assertTrue(AccessoryStore.take(store).isEmpty());
  }

  @ParameterizedTest(name = "holding {0}: shows contents {1}")
  @CsvSource({"0, false", "1, true"})
  @DisplayName("a store shows what it holds with the game's own bundle tooltip")
  void tooltip(final int bottles, final boolean shown) {
    final ItemStack store = pendant();
    if (bottles > 0) {
      AccessoryStore.put(
          List.of(new AccessoryHold(List.of(BloodItems.BLOOD_BOTTLE), 1)),
          store,
          new ItemStack(BloodItems.BLOOD_BOTTLE, bottles));
    }
    assertEquals(shown, AccessoryStore.tooltip(store).isPresent());
    AccessoryStore.tooltip(store)
        .ifPresent(
            data -> assertTrue(data instanceof net.minecraft.item.tooltip.BundleTooltipData));
  }

  @ParameterizedTest(name = "{0} bolts")
  @CsvSource({"64, 64, 0", "128, 128, 0", "150, 128, 22"})
  @DisplayName("a pocket holds more than one stack, up to its own count, and leaves the rest")
  void holdsPastAStack(final int offered, final int held, final int left) {
    final ItemStack belt = new ItemStack(AccessoryItems.of("bandolier"));
    int put = 0;
    for (int i = 0; i < offered; i += 64) {
      final ItemStack bolts = new ItemStack(AmmoItems.STAKE_BOLT, Math.min(64, offered - i));
      put += AccessoryStore.put(AccessoryHoldTest.bandolier(), belt, bolts);
    }
    assertEquals(held, AccessoryStore.held(belt));
    assertEquals(held, put);
    assertEquals(left, offered - put);
  }

  @ParameterizedTest(name = "{0} bolts held: {1} holy water fits")
  @CsvSource({"0, 4", "128, 4"})
  @DisplayName("each pocket keeps its own room, however full the other is")
  void pocketsAreSeparate(final int bolts, final int potions) {
    final ItemStack belt = new ItemStack(AccessoryItems.of("bandolier"));
    for (int i = 0; i < bolts; i += 64) {
      AccessoryStore.put(
          AccessoryHoldTest.bandolier(), belt, new ItemStack(AmmoItems.STAKE_BOLT, 64));
    }
    int put = 0;
    for (int i = 0; i < 6; i++) {
      put +=
          AccessoryStore.put(
              AccessoryHoldTest.bandolier(), belt, AccessoryHoldTest.stack("SPLASH_HOLY"));
    }
    assertEquals(potions, put);
    assertEquals(potions, AccessoryStore.heldIn(AccessoryHoldTest.bandolier().get(1), belt));
  }

  @ParameterizedTest(name = "remove {0} of 70")
  @CsvSource({"1, 69", "64, 6", "70, 0", "80, 0"})
  @DisplayName("removing takes from across stacks until the count is met or the pocket is empty")
  void removes(final int asked, final int left) {
    final ItemStack belt = new ItemStack(AccessoryItems.of("bandolier"));
    AccessoryStore.put(
        AccessoryHoldTest.bandolier(), belt, new ItemStack(AmmoItems.STAKE_BOLT, 64));
    AccessoryStore.put(AccessoryHoldTest.bandolier(), belt, new ItemStack(AmmoItems.STAKE_BOLT, 6));
    assertEquals(
        70 - left, AccessoryStore.remove(belt, stack -> stack.isOf(AmmoItems.STAKE_BOLT), asked));
    assertEquals(left, AccessoryStore.held(belt));
  }

  @ParameterizedTest(name = "wants {0}: {1}")
  @CsvSource({"BOLT, true", "SPLASH_HOLY, true", "GLASS, false"})
  @DisplayName("taking one takes a single item of the kind asked for and leaves the rest")
  void takesOne(final String wanted, final boolean found) {
    final ItemStack belt = new ItemStack(AccessoryItems.of("bandolier"));
    AccessoryStore.put(AccessoryHoldTest.bandolier(), belt, new ItemStack(AmmoItems.STAKE_BOLT, 3));
    AccessoryStore.put(AccessoryHoldTest.bandolier(), belt, AccessoryHoldTest.stack("SPLASH_HOLY"));
    final ItemStack like = AccessoryHoldTest.stack(wanted);
    final var taken =
        AccessoryStore.takeOne(belt, stack -> ItemStack.areItemsAndComponentsEqual(stack, like));
    assertEquals(found, taken.isPresent());
    taken.ifPresent(stack -> assertEquals(1, stack.getCount()));
    assertEquals(found ? 3 : 4, AccessoryStore.held(belt));
  }
}
