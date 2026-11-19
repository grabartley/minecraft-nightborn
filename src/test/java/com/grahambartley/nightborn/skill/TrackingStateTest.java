package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TrackingStateTest {

  @ParameterizedTest(name = "until {0}, now {1}: over {2}")
  @CsvSource({"100, 0, false", "100, 99, false", "100, 100, true", "100, 250, true"})
  @DisplayName("tracking ends when its time runs out")
  void over(final long until, final long now, final boolean over) {
    assertEquals(over, new TrackingState(until, 64, false).over(now));
  }

  @ParameterizedTest(name = "range {0}, blood trails {1}")
  @CsvSource({"64, false", "160, true"})
  @DisplayName("tracking keeps the reach and blood trails it started with")
  void keeps(final int range, final boolean trails) {
    final TrackingState state = new TrackingState(100L, range, trails);
    assertEquals(range, state.range());
    assertEquals(trails, state.bloodTrails());
  }
}
