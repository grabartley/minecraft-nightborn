package com.grahambartley.nightborn.sun;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.stream.Stream;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ParasolItemsTest {

  static Stream<Arguments> facts() {
    final ItemStack parasol = new ItemStack(ParasolItems.PARASOL);
    return Stream.of(
        Arguments.of(
            "item id", "nightborn:parasol", Registries.ITEM.getId(ParasolItems.PARASOL).toString()),
        Arguments.of(
            "open marker id",
            "nightborn:parasol_open",
            Registries.DATA_COMPONENT_TYPE.getId(ParasolItems.OPEN).toString()),
        Arguments.of("stack size", "1", Integer.toString(parasol.getMaxCount())),
        Arguments.of(
            "durability",
            Integer.toString(ParasolWear.DURABILITY),
            Integer.toString(parasol.getMaxDamage())));
  }

  @ParameterizedTest(name = "{0} is {1}")
  @MethodSource("facts")
  @DisplayName("the parasol is one item a stack, as durable as its sun time")
  void registered(final String what, final String expected, final String actual) {
    assertEquals(expected, actual);
  }
}
