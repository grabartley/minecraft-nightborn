package com.grahambartley.nightborn.clothing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.registry.Registries;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ClothingItemsTest {

  private static final Path ASSETS = Path.of("src/main/resources/assets/nightborn");
  private static final Path DATA = Path.of("src/main/resources/data/nightborn");

  @ParameterizedTest(name = "{0}")
  @EnumSource(ClothingPiece.class)
  @DisplayName("each piece is registered at its id with its model, sprite and recipe")
  void registered(final ClothingPiece piece) {
    final ClothingItem item = ClothingItems.of(piece);
    assertEquals("nightborn:" + piece.id(), Registries.ITEM.getId(item).toString());
    assertEquals(piece, item.piece());
    assertTrue(Files.exists(ASSETS.resolve("models/item/" + piece.id() + ".json")));
    assertTrue(Files.exists(ASSETS.resolve("textures/item/" + piece.id() + ".png")));
    assertEquals(
        piece.dyeable(),
        Files.exists(ASSETS.resolve("textures/item/" + piece.id() + "_overlay.png")));
    assertTrue(Files.exists(DATA.resolve("recipe/" + piece.id() + ".json")));
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

  @ParameterizedTest(name = "{0}")
  @EnumSource(ClothingPiece.class)
  @DisplayName("every piece is listed in order")
  void listsEveryPiece(final ClothingPiece piece) {
    assertEquals(ClothingItems.of(piece), ClothingItems.all().get(piece.ordinal()));
  }
}
