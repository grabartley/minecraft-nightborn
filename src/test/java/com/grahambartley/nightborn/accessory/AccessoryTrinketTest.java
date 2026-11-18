package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.clothing.ClothingPowerEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class AccessoryTrinketTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource({"chest", "head", "legs"})
  @DisplayName("an accessory's source never clashes with an armour slot's")
  void sourcesStayApart(final String slot) {
    assertTrue(AccessoryTrinket.SOURCE_PREFIX.startsWith("accessory/"));
    assertTrue(!ClothingPowerEffect.source(slot).startsWith(AccessoryTrinket.SOURCE_PREFIX));
    assertEquals("slot/" + slot, ClothingPowerEffect.source(slot));
  }
}
