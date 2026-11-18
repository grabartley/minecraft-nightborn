package com.grahambartley.nightborn.wheel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WheelChoiceTest {

  @ParameterizedTest(name = "shown {0}, there {1}, alive {2}, in reach {3} -> {4}")
  @CsvSource({
    "true, true, true, true, true",
    "false, true, true, true, false",
    "true, false, false, false, false",
    "true, true, false, true, false",
    "true, true, true, false, false"
  })
  @DisplayName(
      "a choice counts only if it was shown, and its target is still there, alive and near")
  void accepted(
      final boolean shown,
      final boolean there,
      final boolean alive,
      final boolean inReach,
      final boolean accepted) {
    assertEquals(accepted, WheelChoice.accepted(shown, there, alive, inReach));
  }
}
