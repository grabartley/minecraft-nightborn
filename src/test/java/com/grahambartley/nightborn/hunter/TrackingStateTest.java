package com.grahambartley.nightborn.hunter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TrackingStateTest {

  @ParameterizedTest(name = "until {0}, now {1}: over {2}")
  @CsvSource({"100, 0, false", "100, 99, false", "100, 100, true", "100, 250, true"})
  @DisplayName("tracking ends when its time runs out")
  void over(final long until, final long now, final boolean over) {
    assertEquals(over, new TrackingState(until, 64).over(now));
  }
}
