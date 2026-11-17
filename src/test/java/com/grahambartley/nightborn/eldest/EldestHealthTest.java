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

  @ParameterizedTest(
      name = "keeps to a room {0}, had a target {1}, has a target {2}, unhurt {3}: {4}")
  @CsvSource({
    "true, false, true, true, true",
    "true, true, true, true, false",
    "true, false, false, true, false",
    "true, false, true, false, false",
    "false, false, true, true, false"
  })
  @DisplayName(
      "an Eldest in its own room sizes itself to the players near when an unhurt fight begins")
  void rescales(
      final boolean keepsToRoom,
      final boolean hadTarget,
      final boolean hasTarget,
      final boolean unhurt,
      final boolean rescales) {
    assertEquals(rescales, EldestHealth.rescales(keepsToRoom, hadTarget, hasTarget, unhurt));
  }
}
