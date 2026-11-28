package com.grahambartley.nightborn.mob.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BatCourseTest {

  @ParameterizedTest(name = "chasing {0}, target {1}, course {2}, held {3}, ground {4}: {5}")
  @CsvSource({
    "true, true, NaN, false, false, CHASE",
    "true, true, 20, false, true, CHASE",
    "false, false, 20, false, true, FLY_ON",
    "false, false, 20, true, false, FLY_ON",
    "false, true, 20, false, true, LAND",
    "false, false, 2, false, true, LAND",
    "false, false, NaN, false, true, LAND",
    "false, false, 2, true, true, HOVER",
    "false, false, NaN, true, true, HOVER",
    "false, true, NaN, true, true, CHASE",
    "false, true, NaN, false, false, CHASE",
    "false, false, 2, false, false, FLY_ON",
    "false, false, NaN, false, false, HOVER"
  })
  @DisplayName(
      "a bat chases its prey, flies on to where it was headed, keeps its form for the shortest"
          + " hold, and lands only over ground")
  void decides(
      final boolean chasing,
      final boolean hasTarget,
      final double course,
      final boolean held,
      final boolean ground,
      final BatCourse expected) {
    assertEquals(expected, BatCourse.decide(chasing, hasTarget, course, 3.0, held, ground));
  }
}
