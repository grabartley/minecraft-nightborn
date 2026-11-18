package com.grahambartley.nightborn.raid;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NoiseTest {

  @ParameterizedTest(name = "level {0} after {1} ticks at fade {2}: {3}")
  @CsvSource({
    "10, 0, 1, 10",
    "10, 20, 1, 9",
    "10, 100, 1, 5",
    "10, 10, 1, 9.5",
    "3, 200, 1, 0",
    "6, 40, 2, 2",
    "6, 400, 0, 6",
    "6, -20, 1, 6"
  })
  @DisplayName("noise fades by the set amount each second and never drops below silence")
  void fades(final double level, final long ticks, final int fade, final double expected) {
    assertEquals(expected, Noise.faded(level, ticks, fade), 1e-9);
  }

  @ParameterizedTest(name = "level {0}, {1} ticks later, fade {2}, plus {3}: {4}")
  @CsvSource({
    "0, 0, 1, 6, 6",
    "6, 20, 1, 5, 10",
    "4, 40, 1, 2, 4",
    "0, 0, 1, 0, 0",
    "2, 0, 1, -3, 2"
  })
  @DisplayName("a new sound adds to what is left of the last")
  void adds(
      final double level,
      final long ticks,
      final int fade,
      final double loudness,
      final double expected) {
    assertEquals(expected, Noise.heard(level, ticks, fade, loudness), 1e-9);
  }

  @ParameterizedTest(name = "{0} blocks squared within {1}: {2}")
  @CsvSource({"0, 16, true", "256, 16, true", "257, 16, false", "100, 9, false", "81, 9, true"})
  @DisplayName("a sound reaches sleepers up to its reach and no further")
  void carries(final double distanceSquared, final int reach, final boolean carries) {
    assertEquals(carries, Noise.carries(distanceSquared, reach));
  }

  @ParameterizedTest(name = "level {0} against {1}: {2}")
  @CsvSource({"9.99, 10, false", "10, 10, true", "12, 10, true", "0, 1, false"})
  @DisplayName("the house wakes once the noise reaches the threshold")
  void wakes(final double level, final int threshold, final boolean wakes) {
    assertEquals(wakes, Noise.wakes(level, threshold));
  }
}
