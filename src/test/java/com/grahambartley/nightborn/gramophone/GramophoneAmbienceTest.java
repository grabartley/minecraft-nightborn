package com.grahambartley.nightborn.gramophone;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GramophoneAmbienceTest {

  @ParameterizedTest(name = "every {0}s with roll {1}: waits {2}s")
  @CsvSource({"300, 0.0, 150", "300, 0.5, 300", "300, 1.0, 450", "20, 0.0, 10", "1, 0.0, 1"})
  @DisplayName("the first wait is between half and one and a half times the interval")
  void firstWait(final int interval, final double roll, final int wait) {
    assertEquals(wait, GramophoneAmbience.firstWait(interval, roll));
  }

  @ParameterizedTest(name = "wait {0}, player near {1}: {2}")
  @CsvSource({
    "-1, false, -1",
    "40, false, 40",
    "-1, true, 300",
    "40, true, 39",
    "1, true, 0",
    "0, true, 0"
  })
  @DisplayName("the wait only runs down while a player is near, and starts when one arrives")
  void step(final int wait, final boolean near, final int next) {
    assertEquals(next, GramophoneAmbience.step(wait, near, 300, 0.5));
  }

  @ParameterizedTest(name = "wait {0}: due {1}")
  @CsvSource({"-1, false", "0, true", "1, false", "300, false"})
  @DisplayName("the disc starts when the wait reaches zero, never while it is unset")
  void due(final int wait, final boolean due) {
    assertEquals(due, GramophoneAmbience.due(wait));
  }
}
