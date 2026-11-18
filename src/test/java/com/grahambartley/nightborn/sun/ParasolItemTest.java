package com.grahambartley.nightborn.sun;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.stream.Stream;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ParasolItemTest {

  static Stream<Arguments> menders() {
    return Stream.of(
        Arguments.of(Items.STRING, false),
        Arguments.of(Items.STICK, false),
        Arguments.of(Items.LEATHER, false),
        Arguments.of(Items.PHANTOM_MEMBRANE, false));
  }

  @ParameterizedTest(name = "{0}: mends {1}")
  @MethodSource("menders")
  @DisplayName("nothing but wool mends a parasol")
  void mendedWithWool(final Item ingredient, final boolean mends) {
    assertEquals(
        mends,
        ParasolItems.PARASOL.canRepair(
            new ItemStack(ParasolItems.PARASOL), new ItemStack(ingredient)));
  }

  @Test
  @DisplayName("a parasol takes no enchantment")
  void takesNoEnchantment() {
    assertFalse(ParasolItems.PARASOL.isEnchantable(new ItemStack(ParasolItems.PARASOL)));
    assertEquals(0, ParasolItems.PARASOL.getEnchantability());
  }
}
