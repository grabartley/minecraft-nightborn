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
}
