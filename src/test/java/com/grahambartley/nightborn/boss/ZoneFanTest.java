package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ZoneFanTest {

  @ParameterizedTest(name = "{0} lines {1} degrees apart, line {2}: turned {3} degrees")
  @CsvSource({
    "1, 20, 0, 0",
    "3, 12, 0, -12",
    "3, 12, 1, 0",
    "3, 12, 2, 12",
    "2, 10, 0, -5",
    "2, 10, 1, 5"
  })
  @DisplayName("a fan spreads its lines evenly either side of the aim")
  void turns(final int count, final float degrees, final int index, final double turned) {
    assertEquals(Math.toRadians(turned), new ZoneFan(count, degrees).turn(index), 1.0E-9);
  }
}
