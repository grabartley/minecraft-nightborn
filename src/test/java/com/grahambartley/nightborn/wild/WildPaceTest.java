package com.grahambartley.nightborn.wild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WildPaceTest {

  @ParameterizedTest(name = "roamer {0}, target {1}, sleeping {2}: travels {3}")
  @CsvSource({
    "true, false, false, true",
    "false, false, false, false",
    "true, true, false, false",
    "true, false, true, false"
  })
  @DisplayName("a roaming wild vampire travels by day and night while it has no target and wakes")
  void travels(
      final boolean roamer, final boolean target, final boolean sleeping, final boolean travels) {
    assertEquals(travels, WildPace.travels(roamer, target, sleeping));
  }

  @ParameterizedTest(name = "night {0}: legs of {1}")
  @CsvSource({"true, 48", "false, 10"})
  @DisplayName("it travels far by night and makes short hops by day")
  void leg(final boolean night, final int leg) {
    assertEquals(leg, WildPace.leg(night));
  }

  @ParameterizedTest(name = "night {0}, roll {1}: pauses {2} ticks")
  @CsvSource({
    "true, 0, 40",
    "true, 80, 120",
    "true, 81, 40",
    "true, -1, 120",
    "false, 0, 100",
    "false, 200, 300",
    "false, 201, 100"
  })
  @DisplayName("it pauses between legs, longer by day than by night")
  void pause(final boolean night, final int roll, final int ticks) {
    assertEquals(ticks, WildPace.pause(night, roll));
  }

  @ParameterizedTest(name = "night {0}, open sky {1}: may stop {2}")
  @CsvSource({"true, true, true", "true, false, true", "false, false, true", "false, true, false"})
  @DisplayName("by day it only walks and stops where the sky cannot see it")
  void mayStop(final boolean night, final boolean sky, final boolean stops) {
    assertEquals(stops, WildPace.mayStop(night, sky));
  }
}
