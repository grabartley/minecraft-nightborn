package com.grahambartley.nightborn.raid;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MapSpreadTest {

  @ParameterizedTest(name = "spread {0}: scale {1}")
  @CsvSource({"0, 0", "64, 0", "65, 1", "128, 1", "200, 2", "256, 2", "512, 3", "2000, 4"})
  @DisplayName(
      "the map is the smallest vanilla map whose half width covers the spread, so the house"
          + " lies on it")
  void scale(final int spread, final byte scale) {
    assertEquals(scale, MapSpread.scale(spread));
  }
}
