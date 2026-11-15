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

  @ParameterizedTest(name = "{0}: {1}, {2}, {3}, dyeable {4}")
  @CsvSource({
    "TRICORNE, tricorne, HELMET, CLOTH, true",
    "TOP_HAT, top_hat, HELMET, CLOTH, true",
    "MOURNING_VEIL, mourning_veil, HELMET, CLOTH, true",
    "FROCK_COAT, frock_coat, CHESTPLATE, CLOTH, true",
    "HIGH_COLLARED_CAPE, high_collared_cape, CHESTPLATE, CLOTH, true",
    "CORSET, corset, CHESTPLATE, CLOTH, true",
    "DOUBLET, doublet, CHESTPLATE, CLOTH, true",
    "BREECHES, breeches, LEGGINGS, CLOTH, true",
    "TROUSERS, trousers, LEGGINGS, CLOTH, true",
    "LONG_SKIRT, long_skirt, LEGGINGS, CLOTH, true",
    "BUCKLED_SHOES, buckled_shoes, BOOTS, LEATHER, false",
    "RIDING_BOOTS, riding_boots, BOOTS, LEATHER, false"
  })
  @DisplayName("each piece has its id, its armour slot, its fabric and whether it takes dye")
  void facts(
      final ClothingPiece piece,
      final String id,
      final ArmorItem.Type type,
      final ClothingFabric fabric,
      final boolean dyeable) {
    assertEquals(id, piece.id());
    assertEquals(type, piece.type());
    assertEquals(fabric, piece.fabric());
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
    assertEquals(LEATHER.keySet(), perSlot.keySet());
    assertTrue(perSlot.values().stream().allMatch(count -> count >= 2));
    assertTrue(looks >= 10);
  }
}
