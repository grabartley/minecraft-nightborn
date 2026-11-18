package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HunterTargetGoalTest {

  @ParameterizedTest(name = "follow range {0} seen at {1}: {2}")
  @CsvSource({"32, 1.0, 32", "32, 1.5, 48", "32, 0.7, 22.4", "32, 1.05, 33.6"})
  @DisplayName("a hunter keeps a Beacon wearer from further and a Shroud wearer from less far")
  void reach(final double follow, final double seen, final double reach) {
    assertEquals(reach, HunterTargetGoal.reach(follow, seen), 1.0E-9);
  }
}
