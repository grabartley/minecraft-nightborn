package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.Equipment;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class AccessoryItemTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(AccessoryPiece.class)
  @DisplayName("an accessory is not armour, gives no armour, stacks to one and never wears out")
  void notArmour(final AccessoryPiece piece) {
    final AccessoryItem item = AccessoryItems.of(piece);
    final ItemStack stack = new ItemStack(item);
    assertFalse(item instanceof Equipment);
    assertEquals(1, stack.getMaxCount());
    assertFalse(stack.isDamageable());
    assertEquals(piece, item.garment());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(AccessoryPiece.class)
  @DisplayName("a piece shows its own colour until it is dyed")
  void colour(final AccessoryPiece piece) {
    final ItemStack stack = new ItemStack(AccessoryItems.of(piece));
    assertEquals(piece.undyedColor(), AccessoryItems.of(piece).color(stack) & 0xFFFFFF);
    stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x123456, true));
    assertNotEquals(piece.undyedColor(), AccessoryItems.of(piece).color(stack) & 0xFFFFFF);
  }
}
