package com.grahambartley.nightborn.sun;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DirectSunTest {

  @ParameterizedTest(name = "day {0}, open sky {1}, wet {2}, closed coffin {3}: exposed {4}")
  @CsvSource({
    "true, true, false, false, true",
    "false, true, false, false, false",
    "true, false, false, false, false",
    "true, true, true, false, false",
    "true, true, false, true, false"
  })
  @DisplayName(
      "only daylight with open sky reaches a vampire, and shade, rain, water and a shut coffin"
          + " protect")
  void exposure(
      final boolean day,
      final boolean openSky,
      final boolean wet,
      final boolean coffin,
      final boolean exposed) {
    assertEquals(exposed, DirectSun.exposed(day, openSky, wet, coffin));
  }

  @ParameterizedTest(
      name = "day {0}, brightness {1}, open sky {2}, wet {3}, closed coffin {4}: burns {5}")
  @CsvSource({
    "true, 1.0, true, false, false, true",
    "true, 0.51, true, false, false, true",
    "true, 0.5, true, false, false, false",
    "true, 0.4, true, false, false, false",
    "false, 1.0, true, false, false, false",
    "true, 1.0, false, false, false, false",
    "true, 1.0, true, true, false, false",
    "true, 1.0, true, false, true, false"
  })
  @DisplayName("burns on the same terms as the undead: daylight, bright eyes and open sky, dry")
  void burning(
      final boolean day,
      final float brightness,
      final boolean openSky,
      final boolean wet,
      final boolean coffin,
      final boolean burns) {
    assertEquals(burns, DirectSun.burns(day, brightness, openSky, wet, coffin));
  }
}
