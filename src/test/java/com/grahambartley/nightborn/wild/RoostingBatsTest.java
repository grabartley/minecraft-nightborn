package com.grahambartley.nightborn.wild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RoostingBatsTest {

  @ParameterizedTest(name = "air below {0}, two below {1}: hangs {2}")
  @CsvSource({
    "true, true, true",
    "true, false, false",
    "false, true, false",
    "false, false, false"
  })
  @DisplayName("a bat hangs only under a branch or overhang with open air beneath it")
  void hangs(final boolean below, final boolean twoBelow, final boolean hangs) {
    assertEquals(hangs, RoostingBats.hangs(below, twoBelow));
  }
}
