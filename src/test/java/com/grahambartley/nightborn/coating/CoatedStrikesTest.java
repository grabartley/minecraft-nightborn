package com.grahambartley.nightborn.coating;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CoatedStrikesTest {

  @ParameterizedTest(name = "{0} at {1} with {2} resistance: {3}")
  @CsvSource({
    "garlic_touch, 10, 0.3, 7",
    "garlic_touch, 10, 0.0, 10",
    "garlic_touch, 1, 0.8, 1",
    "holy_water, 10, 0.3, 10"
  })
  @DisplayName("Warding dulls a garlic coating but not holy water")
  void weakening(final String source, final int strength, final double resistance, final int out) {
    assertEquals(out, CoatedStrikes.weakening(source, strength, resistance));
  }
}
