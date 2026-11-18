package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.registry.Registries;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class AccessoryItemsTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(AccessoryPiece.class)
  @DisplayName("each piece keeps its old id, so an item from an older world is the same item")
  void registered(final AccessoryPiece piece) {
    final AccessoryItem item = AccessoryItems.of(piece);
    assertEquals("nightborn:" + piece.id(), Registries.ITEM.getId(item).toString());
    assertEquals(piece.ordinal(), AccessoryItems.all().indexOf(item));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(AccessoryPiece.class)
  @DisplayName("every piece wears through the accessory hook, and only dyeable ones wash out")
  void hooks(final AccessoryPiece piece) {
    assertEquals(AccessoryTrinket.INSTANCE, TrinketsApi.getTrinket(AccessoryItems.of(piece)));
    assertEquals(
        piece.dyeable(),
        CauldronBehavior.WATER_CAULDRON_BEHAVIOR.map().get(AccessoryItems.of(piece))
            == CauldronBehavior.CLEAN_DYEABLE_ITEM);
  }
}
