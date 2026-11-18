package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.List;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class WornRefillTest {

  @ParameterizedTest(name = "holds {0} and {1}, used {2}: same kind {3}")
  @CsvSource({
    "SPLASH_HOLY, BOLT, SPLASH_HOLY, true",
    "LINGERING_HOLY, BOLT, SPLASH_HOLY, false",
    "BOLT, LINGERING_HOLY, LINGERING_HOLY, true",
    "BOLT, BOLT, SPLASH_HOLY, false"
  })
  @DisplayName(
      "the hand is refilled with the same kind when the belt has it, else what the pocket holds")
  void like(final String first, final String second, final String used, final boolean same) {
    final List<ItemStack> held =
        List.of(AccessoryHoldTest.stack(first), AccessoryHoldTest.stack(second));
    assertEquals(same, WornRefill.like(held, AccessoryHoldTest.stack(used)));
  }
}
