package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ShadeRulesTest {

  @ParameterizedTest(name = "fixed time {0}, time {1}: sun near {2}")
  @CsvSource({
    "false, 6000, true",
    "false, 12999, true",
    "false, 13000, false",
    "false, 18000, false",
    "false, 21999, false",
    "false, 22000, true",
    "false, 23500, true",
    "false, 30000, true",
    "true, 6000, false",
    "true, 18000, false"
  })
  @DisplayName("the sun is near by day and in the last stretch of night, never without a sun")
  void sunIsNear(final boolean fixedTime, final long time, final boolean near) {
    assertEquals(near, ShadeRules.sunIsNear(fixedTime, time));
  }

  @ParameterizedTest(
      name = "time {0}, open sky {1}, wet {2}, powder snow {3}, closed coffin {4}: seeks {5}")
  @CsvSource({
    "22500, true, false, false, false, true",
    "22500, false, false, false, false, false",
    "22500, true, true, false, false, false",
    "22500, true, false, true, false, false",
    "22500, true, false, false, true, false",
    "6000, true, false, false, false, true",
    "18000, true, false, false, false, false"
  })
  @DisplayName("a vampire seeks shade as dawn nears, unless the sun's own shelters cover it")
  void seeksShade(
      final long time,
      final boolean openSky,
      final boolean wet,
      final boolean snow,
      final boolean coffin,
      final boolean seeks) {
    assertEquals(seeks, ShadeRules.seeksShade(false, time, openSky, wet, snow, coffin));
  }
}
