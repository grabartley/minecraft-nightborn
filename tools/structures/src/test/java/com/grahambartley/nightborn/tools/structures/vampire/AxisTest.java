package com.grahambartley.nightborn.tools.structures.vampire;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AxisTest {

  @ParameterizedTest(name = "{0} maps along {1} across {2} to {3},{4}")
  @CsvSource({"X, 3, 7, 3, 7, west, east, Z", "Z, 3, 7, 7, 3, north, south, X"})
  void mapsAlongAndAcrossToWorldAxes(
      final Axis axis,
      final int along,
      final int across,
      final int x,
      final int z,
      final String low,
      final String high,
      final Axis other) {
    assertEquals(x, axis.x(along, across));
    assertEquals(z, axis.z(along, across));
    assertEquals(low, axis.low());
    assertEquals(high, axis.high());
    assertEquals(other, axis.across());
  }
}
