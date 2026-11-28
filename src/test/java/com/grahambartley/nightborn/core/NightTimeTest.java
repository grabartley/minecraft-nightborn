package com.grahambartley.nightborn.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NightTimeTest {

  @ParameterizedTest(name = "fixed {0}, time {1}: night {2}")
  @CsvSource({
    "false, 6000, false",
    "false, 12999, false",
    "false, 13000, true",
    "false, 18000, true",
    "false, 22999, true",
    "false, 23000, false",
    "false, 42000, true",
    "true, 18000, false"
  })
  @DisplayName(
      "night is the overworld's night, never the Nether or the End, and not a storm at noon")
  void night(final boolean fixed, final long time, final boolean night) {
    assertEquals(night, NightTime.isNight(fixed, time));
  }

  @ParameterizedTest(name = "time {0} falls in night {1}")
  @CsvSource({"0, -1", "12999, -1", "13000, 0", "23000, 0", "36999, 0", "37000, 1", "61000, 2"})
  @DisplayName("a night runs from one dusk to the next, so the day after belongs to it")
  void countsNights(final long time, final long night) {
    assertEquals(night, NightTime.night(time));
  }

  @ParameterizedTest(name = "fixed {0}, time {1}: {2} ticks to dawn")
  @CsvSource({
    "false, 6000, 0",
    "false, 13000, 10000",
    "false, 22000, 1000",
    "false, 22999, 1",
    "false, 23000, 0",
    "false, 46000, 1000",
    "true, 18000, NaN"
  })
  @DisplayName("dawn is counted down through the night, is here by day, and never comes below")
  void untilDawn(final boolean fixed, final long time, final double ticks) {
    assertEquals(ticks, NightTime.untilDawn(fixed, time), 1.0E-9);
  }
}
