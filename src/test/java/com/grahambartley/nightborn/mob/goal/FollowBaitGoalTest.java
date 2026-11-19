package com.grahambartley.nightborn.mob.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FollowBaitGoalTest {

  @ParameterizedTest(name = "hungry {0}, fighting {1}, may be lured {2} -> {3}")
  @CsvSource({
    "true, false, true, true",
    "false, false, true, false",
    "true, true, true, false",
    "true, false, false, false"
  })
  @DisplayName("only a hungry vampire with no fight on comes to bait, and never a boss")
  void drawn(
      final boolean hungry, final boolean fighting, final boolean lured, final boolean drawn) {
    assertEquals(drawn, FollowBaitGoal.drawn(hungry, fighting, lured));
  }

  @ParameterizedTest(name = "distance² {0} -> arrived {1}")
  @CsvSource({"0, true", "2.25, true", "2.3, false", "100, false"})
  @DisplayName("a lured vampire stops once it reaches the bait")
  void arrived(final double distance, final boolean arrived) {
    assertEquals(arrived, FollowBaitGoal.arrived(distance));
  }
}
