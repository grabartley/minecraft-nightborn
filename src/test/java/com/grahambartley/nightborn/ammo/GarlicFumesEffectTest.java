package com.grahambartley.nightborn.ammo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.holy.HolyWater;
import com.grahambartley.nightborn.ward.Wards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class GarlicFumesEffectTest {

  @Test
  @DisplayName("garlic fumes land at once on each breath of the cloud")
  void instant() {
    assertEquals("nightborn:garlic_fumes", GarlicFumesEffect.ENTRY.getIdAsString());
    assertTrue(GarlicFumesEffect.ENTRY.value().isInstant());
    assertEquals(GarlicFumesEffect.COLOUR, GarlicFumesEffect.ENTRY.value().getColor());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        Wards.STRINGS,
        Wards.TOUCH,
        Wards.CRUCIFIX,
        Wards.SHAPE,
        HolyWater.WEAKENED_SOURCE
      })
  @DisplayName("a garlic bomb weakens as its own source, so it stacks with every other")
  void ownSource(final String other) {
    assertNotEquals(other, GarlicFumesEffect.WEAKENED_SOURCE);
  }
}
