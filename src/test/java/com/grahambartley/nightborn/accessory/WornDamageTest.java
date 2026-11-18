package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WornDamageTest {

  @ParameterizedTest(name = "night {0}, shot {1}, unsunned fire {2}: x{3}")
  @CsvSource({
    "false, false, false, 1.0",
    "true, false, false, 0.9",
    "false, true, false, 0.75",
    "false, false, true, 0.5",
    "true, true, false, 0.675",
    "true, false, true, 0.45"
  })
  @DisplayName("each worn reduction applies only to its own kind of harm")
  void factor(final boolean night, final boolean shot, final boolean fire, final double factor) {
    assertEquals(factor, WornDamage.factor(night, 0.9, shot, 0.75, fire, 0.5), 1.0E-9);
  }

  @ParameterizedTest(name = "fire {0}, burning {1}, sun-lit {2}: {3}")
  @CsvSource({
    "true, true, true, false",
    "true, true, false, true",
    "true, false, true, true",
    "false, false, false, false"
  })
  @DisplayName("burning the sun lit is never softened, but a flame or lava touched in the sun is")
  void unsunnedFire(
      final boolean fire, final boolean burning, final boolean sunLit, final boolean softened) {
    assertEquals(softened, WornDamage.unsunnedFire(fire, burning, sunLit));
  }
}
