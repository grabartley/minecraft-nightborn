package com.grahambartley.nightborn.clothing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ClothingPieceTest {

  private static final Map<ArmorItem.Type, Item> LEATHER =
      Map.of(
          ArmorItem.Type.HELMET, Items.LEATHER_HELMET,
          ArmorItem.Type.CHESTPLATE, Items.LEATHER_CHESTPLATE,
          ArmorItem.Type.LEGGINGS, Items.LEATHER_LEGGINGS,
          ArmorItem.Type.BOOTS, Items.LEATHER_BOOTS);

  @ParameterizedTest(name = "{0}: {1}, {2}, dyeable {3}")
  @CsvSource({
    "TRICORNE, tricorne, HELMET, true",
    "TOP_HAT, top_hat, HELMET, true",
    "MOURNING_VEIL, mourning_veil, HELMET, true",
    "FROCK_COAT, frock_coat, CHESTPLATE, true",
    "HIGH_COLLARED_CAPE, high_collared_cape, CHESTPLATE, true",
    "CORSET, corset, CHESTPLATE, true",
    "DOUBLET, doublet, CHESTPLATE, true",
    "BREECHES, breeches, LEGGINGS, true",
    "TROUSERS, trousers, LEGGINGS, true",
    "LONG_SKIRT, long_skirt, LEGGINGS, true",
    "BUCKLED_SHOES, buckled_shoes, BOOTS, false",
    "RIDING_BOOTS, riding_boots, BOOTS, false"
  })
  @DisplayName("each piece has its id, its armour slot and whether it takes dye")
  void facts(
      final ClothingPiece piece,
      final String id,
      final ArmorItem.Type type,
      final boolean dyeable) {
    assertEquals(id, piece.id());
    assertEquals(type, piece.type());
    assertEquals(dyeable, piece.dyeable());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(ClothingPiece.class)
  @DisplayName("each piece wears out as fast as the leather piece for its slot")
  void wearsLikeLeather(final ClothingPiece piece) {
    assertEquals(LEATHER.get(piece.type()).getDefaultStack().getMaxDamage(), piece.durability());
  }

  @Test
  @DisplayName("every armour slot has enough pieces to dress ten distinct looks")
  void dressesTenLooks() {
    final Map<ArmorItem.Type, Long> perSlot =
        Arrays.stream(ClothingPiece.values())
            .collect(Collectors.groupingBy(ClothingPiece::type, Collectors.counting()));
    final long looks = perSlot.values().stream().reduce(1L, (a, b) -> a * b);
    assertEquals(ArmorItem.Type.values().length - 1, perSlot.size());
    assertTrue(perSlot.values().stream().allMatch(count -> count >= 2));
    assertTrue(looks >= 10);
  }
}
