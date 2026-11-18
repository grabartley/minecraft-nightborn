package com.grahambartley.nightborn.sun;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SunFireTest {

  @ParameterizedTest(name = "now {0}, lit until {1}: {2}")
  @CsvSource({
    "100, 260, true",
    "259, 260, true",
    "260, 260, false",
    "100, -9223372036854775808, false"
  })
  @DisplayName("burning counts as the sun's only while the fire it lit lasts")
  void litBySun(final long now, final long until, final boolean sun) {
    assertEquals(sun, SunFire.litBySun(now, until));
  }
}
