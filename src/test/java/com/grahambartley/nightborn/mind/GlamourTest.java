package com.grahambartley.nightborn.mind;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GlamourTest {

  @ParameterizedTest(name = "until {0}, now {1}: lasts {2}")
  @CsvSource({"600, 0, true", "600, 599, true", "600, 600, false", "600, 900, false"})
  @DisplayName("a glamour lasts until its time")
  void lasts(final long until, final long now, final boolean lasts) {
    assertEquals(lasts, Glamour.lasts(until, now));
  }
}
