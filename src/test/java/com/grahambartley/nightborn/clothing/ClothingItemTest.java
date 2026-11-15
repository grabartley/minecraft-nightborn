package com.grahambartley.nightborn.clothing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ClothingItemTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(ClothingPiece.class)
  @DisplayName("each piece is cloth worn in its own armour slot, one at a time")
  void wornInItsSlot(final ClothingPiece piece) {
    final ClothingItem item = ClothingItems.of(piece);
    assertEquals(piece.type(), item.getType());
    assertEquals(piece.type().getEquipmentSlot(), item.getSlotType());
    assertEquals(ClothingMaterial.CLOTH, item.getMaterial());
    assertEquals(1, item.getMaxCount());
    assertEquals(piece.durability(), new ItemStack(item).getMaxDamage());
  }
}
