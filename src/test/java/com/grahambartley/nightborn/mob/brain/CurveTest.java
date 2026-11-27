package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CurveTest {

  @ParameterizedTest(name = "{0} {1} in {2}..{3} = {4}")
  @CsvSource({
    "BETWEEN, 3.9, 4, 20, 0",
    "BETWEEN, 4, 4, 20, 1",
    "BETWEEN, 20, 4, 20, 1",
    "BETWEEN, 20.1, 4, 20, 0",
    "RISING, 0, 0, 10, 0",
    "RISING, 5, 0, 10, 0.5",
    "RISING, 12, 0, 10, 1",
    "RISING, -1, 0, 10, 0",
    "RISING, 3, 3, 3, 1",
    "RISING, 2.9, 3, 3, 0",
    "FALLING, 0, 0, 10, 1",
    "FALLING, 2.5, 0, 10, 0.75",
    "FALLING, 10, 0, 10, 0",
    "BETWEEN, NaN, 0, 10, 0",
    "RISING, NaN, 0, 10, 0",
    "FALLING, NaN, 0, 10, 0"
  })
  @DisplayName("a curve turns a fact into a value from 0 to 1, and a missing fact into 0")
  void value(
      final Curve curve, final double x, final double low, final double high, final double value) {
    assertEquals(value, curve.value(x, low, high), 1.0E-9);
  }
}
