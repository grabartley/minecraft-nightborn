package com.grahambartley.nightborn.faith;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TimedMarkTest {

  @ParameterizedTest(name = "now {0}, until {1} -> {2}")
  @CsvSource({"0, 40, true", "39, 40, true", "40, 40, false", "41, 40, false"})
  @DisplayName("a mark holds until its time is up")
  void marked(final long now, final long until, final boolean marked) {
    assertEquals(marked, TimedMark.marked(now, until));
  }
}
