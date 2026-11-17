package com.grahambartley.nightborn.eldest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EldestHealthTest {

  @ParameterizedTest(name = "{0} players at {1}% each add {2}")
  @CsvSource({
    "0, 50, 0.0",
    "1, 50, 0.0",
    "2, 50, 0.5",
    "4, 50, 1.5",
    "3, 0, 0.0",
    "3, 100, 2.0",
    "3, -20, 0.0"
  })
  @DisplayName("each player after the first adds a share of the Eldest's health")
  void bonus(final int players, final int percent, final double bonus) {
    assertEquals(bonus, EldestHealth.bonus(players, percent), 1.0e-9);
  }
}
