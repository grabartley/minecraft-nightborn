package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BatFormPoseTest {

  @ParameterizedTest(name = "hanging {0}, on ground {1}, swing {2}, sprinting {3} -> {4}")
  @CsvSource({
    "true, false, 0.0, false, HANG",
    "true, true, 1.0, true, HANG",
    "false, true, 0.0, false, REST",
    "false, true, 0.05, false, REST",
    "false, true, 0.06, false, CRAWL",
    "false, true, 1.0, true, CRAWL",
    "false, false, 0.0, false, FLY",
    "false, false, 1.0, false, FLY",
    "false, false, 1.0, true, SWOOP"
  })
  @DisplayName("a bat hangs, rests or crawls on the ground, flies, and swoops at a sprint")
  void of(
      final boolean hanging,
      final boolean onGround,
      final float swing,
      final boolean sprinting,
      final BatFormPose expected) {
    assertEquals(expected, BatFormPose.of(hanging, onGround, swing, sprinting));
  }
}
