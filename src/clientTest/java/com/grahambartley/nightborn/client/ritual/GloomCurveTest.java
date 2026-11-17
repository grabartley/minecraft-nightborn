package com.grahambartley.nightborn.client.ritual;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GloomCurveTest {

  @ParameterizedTest(name = "tick {0} of a {1} tick build-up -> {2}")
  @CsvSource({
    "-1, 200, 0.0",
    "0, 200, 0.0",
    "100, 200, 0.5",
    "199, 200, 0.995",
    "200, 200, 1.0",
    "300, 200, 0.5",
    "400, 200, 0.0",
    "5000, 200, 0.0",
    "0, 0, 1.0"
  })
  @DisplayName("the gloom thickens through the build-up, then lifts")
  void strength(final long elapsed, final int buildUp, final float strength) {
    assertEquals(strength, GloomCurve.strength(elapsed, buildUp), 1.0e-4F);
  }

  @ParameterizedTest(name = "{0} blocks away with range {1} -> {2}")
  @CsvSource({
    "0, 32, 1.0",
    "16, 32, 1.0",
    "24, 32, 0.5",
    "32, 32, 0.0",
    "60, 32, 0.0",
    "5, 0, 0.0"
  })
  @DisplayName("the gloom is full near the altar and fades out at its range")
  void reach(final double distance, final int range, final float reach) {
    assertEquals(reach, GloomCurve.reach(distance, range), 1.0e-4F);
  }
}
