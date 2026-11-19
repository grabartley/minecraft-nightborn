package com.grahambartley.nightborn.client.faith;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FlameLifeTest {

  private static final float EPSILON = 1.0E-4F;

  @ParameterizedTest(name = "age {0} of {1}: alpha {2}")
  @CsvSource({"0, 10, 0.95", "6, 10, 0.95", "8, 10, 0.5428571", "10, 10, 0.0"})
  @DisplayName("a flame burns at full strength, then gutters out over its last third")
  void alpha(final float age, final int maxAge, final float alpha) {
    assertEquals(alpha, FlameLife.alpha(age, maxAge), EPSILON);
  }

  @ParameterizedTest(name = "age {0} of {1}: scale {2}")
  @CsvSource({"0, 10, 1.0", "5, 10, 0.725", "10, 10, 0.45"})
  @DisplayName("a flame shrinks as it rises")
  void scale(final float age, final int maxAge, final float scale) {
    assertEquals(scale, FlameLife.scale(age, maxAge), EPSILON);
  }
}
