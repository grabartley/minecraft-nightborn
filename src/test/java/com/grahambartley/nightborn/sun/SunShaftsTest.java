package com.grahambartley.nightborn.sun;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SunShaftsTest {

  @ParameterizedTest(name = "lit until {0}, now {1}: lit {2}")
  @CsvSource({"100, 99, true", "100, 100, false", "100, 140, false", "0, -5, true"})
  @DisplayName("a shaft lights a vampire only until its time runs out")
  void lit(final long until, final long now, final boolean lit) {
    assertEquals(lit, SunShafts.litAt(until, now));
  }

  @ParameterizedTest(name = "now {0}")
  @CsvSource({"0", "24000"})
  @DisplayName("a vampire never touched by a shaft is not lit")
  void neverLit(final long now) {
    assertEquals(false, SunShafts.litAt(null, now));
  }
}
