package com.grahambartley.nightborn.sun;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DirectSunTest {

  @ParameterizedTest(
      name = "day {0}, open sky {1}, wet {2}, powder snow {3}, closed coffin {4}: burns {5}")
  @CsvSource({
    "true, true, false, false, false, true",
    "false, true, false, false, false, false",
    "true, false, false, false, false, false",
    "true, true, true, false, false, false",
    "true, true, false, true, false, false",
    "true, true, false, false, true, false"
  })
  @DisplayName(
      "only daylight with open sky burns, and shade, rain, water and a shut coffin protect")
  void exposure(
      final boolean day,
      final boolean openSky,
      final boolean wet,
      final boolean snow,
      final boolean coffin,
      final boolean burns) {
    assertEquals(burns, DirectSun.exposed(day, openSky, wet, snow, coffin));
  }
}
