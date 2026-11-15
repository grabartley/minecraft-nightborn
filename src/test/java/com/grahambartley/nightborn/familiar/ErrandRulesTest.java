package com.grahambartley.nightborn.familiar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ErrandRulesTest {

  @ParameterizedTest(name = "fetch here {0}, deliver here {1}, {2} and {3} away, range {4}: {5}")
  @CsvSource({
    "true, true, 0, 0, 32, true",
    "true, true, 1024, 1024, 32, true",
    "true, true, 1025, 0, 32, false",
    "true, true, 0, 1025, 32, false",
    "false, true, 0, 0, 32, false",
    "true, false, 0, 0, 32, false"
  })
  @DisplayName("an errand runs only when both chests are in this world and in range")
  void inReach(
      final boolean fetchHere,
      final boolean deliverHere,
      final double fetchDistance,
      final double deliverDistance,
      final int range,
      final boolean reach) {
    assertEquals(
        reach, ErrandRules.inReach(fetchHere, deliverHere, fetchDistance, deliverDistance, range));
  }
}
