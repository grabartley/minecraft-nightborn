package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.clothing.ClothingPowerEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class AccessoryTrinketTest {

  @ParameterizedTest(name = "{0}/0 vs armour {1}")
  @CsvSource({"chest/back, chest", "head/face, head", "legs/belt, legs"})
  @DisplayName("each accessory slot is its own source, never the armour slot it sits over")
  void sourcesStayApart(final String slot, final String armour) {
    assertEquals("accessory/" + slot + "/0", AccessoryTrinket.source(slot + "/0"));
    assertNotEquals(ClothingPowerEffect.source(armour), AccessoryTrinket.source(slot + "/0"));
  }
}
