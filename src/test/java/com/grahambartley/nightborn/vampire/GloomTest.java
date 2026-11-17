package com.grahambartley.nightborn.vampire;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GloomTest {

  @ParameterizedTest(name = "{0} ticks at {1} resistance: {2}")
  @CsvSource({
    "200, 0.5, 100",
    "200, 0, 200",
    "200, 0.8, 40",
    "1, 0.5, 1",
    "3, 0.5, 2",
    "200, 4, 1"
  })
  @DisplayName("gloom lasts a shorter time the more it is resisted, and never less than a tick")
  void shortened(final int ticks, final double resistance, final int left) {
    assertEquals(left, Gloom.shortened(ticks, resistance));
  }
}
