package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.List;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class AccessoryOwnershipTest {

  @ParameterizedTest(name = "{0} and {1}: {2} kinds")
  @CsvSource({
    "ring_of_the_court, ring_of_the_beast, 2",
    "ring_of_the_court, ring_of_the_court, 1",
    "ring_of_the_court, '', 1"
  })
  @DisplayName("ownership counts each kind of piece once and skips empty slots")
  void collect(final String first, final String second, final int kinds) {
    final List<ItemStack> stacks =
        List.of(
            new ItemStack(AccessoryItems.of(first)),
            second.isEmpty() ? ItemStack.EMPTY : new ItemStack(AccessoryItems.of(second)),
            ItemStack.EMPTY);
    assertEquals(kinds, AccessoryOwnership.collect(stacks).size());
    assertTrue(AccessoryOwnership.collect(stacks).contains(AccessoryItems.of(first)));
  }
}
