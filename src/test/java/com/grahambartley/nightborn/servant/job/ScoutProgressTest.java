package com.grahambartley.nightborn.servant.job;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ScoutProgressTest {

  @ParameterizedTest(name = "best {0}, {1} ticks since, now {2} -> best {3}, since {4}")
  @CsvSource({
    "100, 40, 90, 90, 0",
    "100, 40, 100, 100, 60",
    "100, 40, 120, 100, 60",
    "1.7976931348623157E308, 0, 500, 500, 0"
  })
  @DisplayName("a scout's progress resets each time it gets closer than ever before")
  void step(
      final double best,
      final int since,
      final double now,
      final double keptBest,
      final int keptSince) {
    assertEquals(
        new ScoutProgress(keptBest, keptSince), new ScoutProgress(best, since).step(now, 20));
  }

  @ParameterizedTest(name = "{0} ticks without getting closer -> lost {1}")
  @CsvSource({"0, false", "1199, false", "1200, true", "5000, true"})
  @DisplayName("a scout that stops getting closer for long enough has lost its way")
  void lost(final int since, final boolean lost) {
    assertEquals(lost, new ScoutProgress(10.0, since).lost(1200));
  }

  @ParameterizedTest(name = "{0} squared away, sight {1} -> {2}")
  @CsvSource({"0, 24, true", "576, 24, true", "577, 24, false", "9, 3, true", "10, 3, false"})
  @DisplayName("a scout has arrived once the place is within its sight")
  void arrived(final double distanceSquared, final int sight, final boolean arrived) {
    assertEquals(arrived, ScoutProgress.arrived(distanceSquared, sight));
  }
}
