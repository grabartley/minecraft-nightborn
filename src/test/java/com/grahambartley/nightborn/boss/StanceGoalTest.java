package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StanceGoalTest {

  @ParameterizedTest(name = "at tick {0}, with no path {1}: finds a path {2}")
  @CsvSource({"0, false, true", "5, false, false", "5, true, true", "10, false, true"})
  @DisplayName("a boss closing in finds a new path when it has none and every so often")
  void repaths(final int age, final boolean idle, final boolean repaths) {
    assertEquals(repaths, StanceGoal.repaths(age, idle));
  }
}
