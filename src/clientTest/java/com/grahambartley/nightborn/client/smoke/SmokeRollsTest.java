package com.grahambartley.nightborn.client.smoke;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SmokeRollsTest {

  @ParameterizedTest(name = "roll {0}: lives {1} ticks")
  @CsvSource({"0.0, 30", "0.5, 40", "0.9999, 49", "1.0, 49", "-1.0, 30", "2.0, 49"})
  @DisplayName("a puff lives 30 to 49 ticks")
  void maxAge(final float roll, final int expected) {
    assertEquals(expected, SmokeRolls.maxAge(roll));
  }

  @ParameterizedTest(name = "roll {0}: size {1}")
  @CsvSource({"0.0, 0.4", "0.5, 0.525", "1.0, 0.65", "-1.0, 0.4", "2.0, 0.65"})
  @DisplayName("a puff grows to between 0.4 and 0.65")
  void fullSize(final float roll, final float expected) {
    assertEquals(expected, SmokeRolls.fullSize(roll), 1.0e-4F);
  }

  @ParameterizedTest(name = "roll {0}: shade {1}")
  @CsvSource({"0.0, 0.8", "0.5, 0.9", "1.0, 1.0", "-1.0, 0.8", "2.0, 1.0"})
  @DisplayName("a puff is shaded between 0.8 and full brightness")
  void shade(final float roll, final float expected) {
    assertEquals(expected, SmokeRolls.shade(roll), 1.0e-4F);
  }
}
