package com.grahambartley.nightborn.hunter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FaithBoostTest {

  @ParameterizedTest(name = "faith {0}, bonus {1}%: x{2}")
  @CsvSource({
    "0, 25, 1.0",
    "1, 25, 1.25",
    "2, 25, 1.5",
    "10, 25, 3.5",
    "2, 0, 1.0",
    "-3, 25, 1.0",
    "2, -10, 1.0",
    "0.5, 50, 1.25"
  })
  @DisplayName("each point of faith adds the bonus share, never less than no faith")
  void multiplier(final double faith, final int bonus, final double expected) {
    assertEquals(expected, FaithBoost.multiplier(faith, bonus), 1.0e-9);
  }
}
