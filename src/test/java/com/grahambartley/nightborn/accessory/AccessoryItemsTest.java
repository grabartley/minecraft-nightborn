package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.registry.Registries;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class AccessoryItemsTest {

  @ParameterizedTest(name = "{0}")
  @MethodSource("com.grahambartley.nightborn.accessory.AccessoriesTest#ids")
  @DisplayName("each piece keeps its id, so an item from an older world is the same item")
  void registered(final String id) {
    final AccessoryItem item = AccessoryItems.of(id);
    assertEquals("nightborn:" + id, Registries.ITEM.getId(item).toString());
    assertEquals(AccessoryItems.IDS.indexOf(id), AccessoryItems.all().indexOf(item));
    assertEquals(id, item.artId());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("com.grahambartley.nightborn.accessory.AccessoriesTest#ids")
  @DisplayName("every piece wears through the accessory hook, and a cauldron washes any dye out")
  void hooks(final String id) {
    assertEquals(AccessoryTrinket.INSTANCE, TrinketsApi.getTrinket(AccessoryItems.of(id)));
    assertEquals(
        CauldronBehavior.CLEAN_DYEABLE_ITEM,
        CauldronBehavior.WATER_CAULDRON_BEHAVIOR.map().get(AccessoryItems.of(id)));
  }

  @Test
  @DisplayName("asking for a piece that does not exist fails loudly")
  void unknown() {
    assertThrows(IllegalArgumentException.class, () -> AccessoryItems.of("no_such_piece"));
  }
}
