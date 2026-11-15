package com.grahambartley.nightborn.clothing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.registry.Registries;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ClothingItemsTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(ClothingPiece.class)
  @DisplayName("each piece is registered at its id and listed in slot order")
  void registered(final ClothingPiece piece) {
    final ClothingItem item = ClothingItems.of(piece);
    assertEquals("nightborn:" + piece.id(), Registries.ITEM.getId(item).toString());
    assertEquals(piece, item.piece());
    assertEquals(piece.ordinal(), ClothingItems.all().indexOf(item));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(ClothingPiece.class)
  @DisplayName("a water cauldron washes the dye out of dyeable pieces only")
  void cauldronWashesDyeablePieces(final ClothingPiece piece) {
    final boolean washes =
        CauldronBehavior.WATER_CAULDRON_BEHAVIOR.map().get(ClothingItems.of(piece))
            == CauldronBehavior.CLEAN_DYEABLE_ITEM;
    assertEquals(piece.dyeable(), washes);
  }
}
