package com.grahambartley.nightborn.clothing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ClothingItemTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(ClothingPiece.class)
  @DisplayName("each piece is worn in its own armour slot, one at a time, in its own fabric")
  void wornInItsSlot(final ClothingPiece piece) {
    final ClothingItem item = ClothingItems.of(piece);
    assertEquals(piece.type(), item.getType());
    assertEquals(piece.type().getEquipmentSlot(), item.getSlotType());
    assertEquals(ClothingMaterial.of(piece.fabric()), item.getMaterial());
    assertEquals(1, item.getMaxCount());
    assertEquals(piece.durability(), new ItemStack(item).getMaxDamage());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(ClothingPiece.class)
  @DisplayName("an undyed piece shows its own colour and a dyed one shows the dye")
  void showsItsColour(final ClothingPiece piece) {
    final ClothingItem item = ClothingItems.of(piece);
    final ItemStack stack = new ItemStack(item);
    assertEquals(piece.undyedColor(), item.color(stack, null) & 0xFFFFFF);
    stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0xB02E26, true));
    assertEquals(0xB02E26, item.color(stack, null) & 0xFFFFFF);
  }
}
