package com.grahambartley.nightborn.inquisitor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StakeDownedGoalTest {

  @ParameterizedTest(name = "vampire {0}, helpless {1}, {2} blocks away: stakes {3}")
  @CsvSource({
    "true, true, 2.0, true",
    "true, true, 2.5, true",
    "true, true, 2.6, false",
    "true, false, 1.0, false",
    "false, true, 1.0, false"
  })
  @DisplayName("he stakes a downed vampire once he stands over it")
  void stakes(
      final boolean vampire, final boolean helpless, final double distance, final boolean stakes) {
    assertEquals(stakes, StakeDownedGoal.stakes(vampire, helpless, distance));
  }
}
