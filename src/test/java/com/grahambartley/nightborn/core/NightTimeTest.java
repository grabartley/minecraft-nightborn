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
}
