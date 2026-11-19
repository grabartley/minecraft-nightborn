package com.grahambartley.nightborn.trap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TrapSpotsTest {

  @ParameterizedTest(name = "floor {0}, open {1}, wet {2}, burning {3} -> {4}")
  @CsvSource({
    "true, true, false, false, true",
    "false, true, false, false, false",
    "true, false, false, false, false",
    "true, true, true, false, false",
    "true, true, false, true, false"
  })
  @DisplayName("a trap goes only on solid floor with open space, never in water or fire")
  void fits(
      final boolean floor,
      final boolean open,
      final boolean wet,
      final boolean burning,
      final boolean fits) {
    assertEquals(fits, TrapSpots.fits(floor, open, wet, burning));
  }

  @ParameterizedTest(name = "distance² {0}, range {1} -> {2}")
  @CsvSource({"16, 4, true", "16.1, 4, false", "0, 4, true"})
  @DisplayName("a trap is set no further than its reach")
  void inRange(final double distance, final float range, final boolean inRange) {
    assertEquals(inRange, TrapSpots.inRange(distance, range));
  }
}
