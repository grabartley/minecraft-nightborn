package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VampireBatPoseTest {

  @ParameterizedTest(name = "roosting {0}, biting {1}: {2}")
  @CsvSource({"true, true, HANG, hang", "false, true, BITE, bite", "false, false, FLY, fly"})
  @DisplayName("a roosting bat hangs, and a flying bat bites or flies")
  void pose(
      final boolean roosting,
      final boolean biting,
      final VampireBatPose pose,
      final String animation) {
    assertEquals(pose, VampireBatPose.of(roosting, biting));
    assertEquals(animation, pose.animation());
  }
}
