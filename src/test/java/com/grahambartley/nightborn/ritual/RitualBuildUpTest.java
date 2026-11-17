package com.grahambartley.nightborn.ritual;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RitualBuildUpTest {

  @ParameterizedTest(name = "tick {0} of {1}")
  @CsvSource({
    "0, 200, 0.0, false, true, 0.9, 1",
    "40, 200, 0.2, false, true, 0.82, 3",
    "100, 200, 0.5, false, false, 0.7, 7",
    "199, 200, 0.995, false, false, 0.502, 12",
    "200, 200, 1.0, true, false, 0.5, 12",
    "260, 200, 1.0, true, false, 0.5, 12",
    "0, 0, 1.0, true, false, 0.5, 12"
  })
  @DisplayName("the build-up grows, tolls lower and ends on time")
  void grows(
      final int elapsed,
      final int total,
      final float progress,
      final boolean done,
      final boolean tolls,
      final float pitch,
      final int smoke) {
    final RitualBuildUp at = new RitualBuildUp(elapsed, total);
    assertEquals(progress, at.progress(), 1.0e-4F);
    assertEquals(done, at.done());
    assertEquals(tolls, at.tolls());
    assertEquals(pitch, at.tollPitch(), 1.0e-3F);
    assertEquals(smoke, at.smoke());
  }

  @ParameterizedTest(name = "tick {0}, roll {1} -> {2}")
  @CsvSource({
    "0, 0.0, false",
    "100, 0.05, true",
    "100, 0.1, false",
    "200, 0.19, true",
    "200, 0.2, false"
  })
  @DisplayName("candles gutter more often as the ritual builds")
  void gutters(final int elapsed, final double roll, final boolean gutters) {
    assertEquals(gutters, new RitualBuildUp(elapsed, 200).gutters(roll));
  }
}
