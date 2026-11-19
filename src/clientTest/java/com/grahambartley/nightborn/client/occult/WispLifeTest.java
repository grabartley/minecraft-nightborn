package com.grahambartley.nightborn.client.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WispLifeTest {

  @ParameterizedTest(name = "age {0} of {1}: alpha {2}")
  @CsvSource({
    "0, 20, 0.0",
    "2, 20, 0.275",
    "4, 20, 0.55",
    "12, 20, 0.275",
    "20, 20, 0.0",
    "30, 20, 0.0",
    "-2, 20, 0.0",
    "0, 0, 0.0"
  })
  @DisplayName("a wisp fades in quickly, then thins away over the rest of its life")
  void alpha(final float age, final int maxAge, final float expected) {
    assertEquals(expected, WispLife.alpha(age, maxAge), 1.0e-4F);
  }

  @ParameterizedTest(name = "age {0} of {1}: scale {2}")
  @CsvSource({"0, 20, 1.0", "10, 20, 0.675", "20, 20, 0.35", "40, 20, 0.35"})
  @DisplayName("a wisp shrinks as it rises")
  void scale(final float age, final int maxAge, final float expected) {
    assertEquals(expected, WispLife.scale(age, maxAge), 1.0e-4F);
  }
}
