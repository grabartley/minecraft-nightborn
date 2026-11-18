package com.grahambartley.nightborn.eldest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EldestHealthTest {

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
