package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BossAmbienceTest {

  @ParameterizedTest(name = "next at {0}, now {1}: due {2}")
  @CsvSource({"-1, 500, false", "600, 500, false", "600, 600, true", "600, 900, true"})
  @DisplayName("a cue plays once its time has come, and never before one is set")
  void due(final long nextAt, final long now, final boolean due) {
    assertEquals(due, BossAmbience.due(nextAt, now));
  }
}
