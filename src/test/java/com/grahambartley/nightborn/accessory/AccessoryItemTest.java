package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.Equipment;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class AccessoryItemTest {

  @ParameterizedTest(name = "{0}")
  @MethodSource("com.grahambartley.nightborn.accessory.AccessoriesTest#ids")
  @DisplayName("an accessory is not armour, gives no armour, stacks to one and never wears out")
  void notArmour(final String id) {
    final AccessoryItem item = AccessoryItems.of(id);
    final ItemStack stack = new ItemStack(item);
    assertFalse(item instanceof Equipment);
    assertEquals(1, stack.getMaxCount());
    assertFalse(stack.isDamageable());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("com.grahambartley.nightborn.accessory.AccessoriesTest#ids")
  @DisplayName("with no data to read, a piece is undyed white, and a dye shows through")
  void colourWithoutData(final String id) {
    final AccessoryItem item = AccessoryItems.of(id);
    final ItemStack stack = new ItemStack(item);
    assertEquals(Accessory.UNDYED, item.color(stack, null) & 0xFFFFFF);
    stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x123456, true));
    assertEquals(0x123456, item.color(stack, null) & 0xFFFFFF);
  }
}
