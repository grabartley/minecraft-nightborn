package com.grahambartley.nightborn.servant.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class KeepCloseGoalTest {

  @ParameterizedTest(name = "distance {0}, limit {1}: strays {2}")
  @CsvSource({"13, 12, true", "12, 12, false", "3, 4, false"})
  @DisplayName("a servant goes back to its master once it strays past the limit")
  void strays(final double distance, final double limit, final boolean strays) {
    assertEquals(strays, KeepCloseGoal.strays(distance, limit));
  }
}
