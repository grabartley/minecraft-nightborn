package com.grahambartley.nightborn.familiar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FamiliarClockTest {

  @ParameterizedTest(name = "time {0} is day {1}")
  @CsvSource({"0, 0", "23999, 0", "24000, 1", "6000, 0", "1000000, 41", "-1, -1"})
  @DisplayName("a familiar counts whole days from the world clock")
  void dayOf(final long time, final long day) {
    assertEquals(day, FamiliarClock.dayOf(time));
  }
}
