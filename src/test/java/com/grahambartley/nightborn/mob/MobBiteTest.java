package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MobBiteTest {

  @ParameterizedTest(name = "roll {0}, chance {1}: {2}")
  @CsvSource({"0.1, 0.15, true", "0.15, 0.15, false", "0.5, 0.15, false", "0.0, 0.0, false"})
  @DisplayName("a worn gift breaks a vampire's bite on its share of rolls")
  void breaks(final float roll, final double chance, final boolean breaks) {
    assertEquals(breaks, MobBite.breaks(roll, chance));
  }
}
