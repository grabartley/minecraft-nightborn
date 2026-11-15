package com.grahambartley.nightborn.sickness;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SicknessClockTest {

  @ParameterizedTest(name = "clock {0} then {1}: {2} ticks")
  @CsvSource({
    "1000, 1001, 1",
    "1000, 1000, 1",
    "13000, 24000, 11000",
    "24000, 1000, 1",
    "0, 4294967296, 2147483647"
  })
  @DisplayName("each tick counts at least once, and a skipped night counts in full")
  void advances(final long first, final long second, final int expected) {
    final SicknessClock clock = new SicknessClock();
    clock.advance(first);
    assertEquals(expected, clock.advance(second));
  }

  @Test
  @DisplayName("the first reading after a start or reset counts one tick")
  void firstReadingCountsOne() {
    final SicknessClock clock = new SicknessClock();
    assertEquals(1, clock.advance(50000));
    clock.reset();
    assertEquals(1, clock.advance(90000));
  }

  @ParameterizedTest(name = "{0} run + step {1} = {2}")
  @CsvSource({
    "0, 1, 1",
    "100, 11000, 11100",
    "100, 0, 101",
    "100, -4, 101",
    "2147483000, 100000, 2147483647"
  })
  @DisplayName("the time run grows by the step and never overflows")
  void run(final int ticksRun, final int step, final int expected) {
    assertEquals(expected, SicknessClock.run(ticksRun, step));
  }
}
