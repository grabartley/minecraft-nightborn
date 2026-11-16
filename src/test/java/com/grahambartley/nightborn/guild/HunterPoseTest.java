package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HunterPoseTest {

  @ParameterizedTest(name = "swing {0}, trade {1}, sprint {2}, limb swing {3}: {4}")
  @CsvSource({
    "true, true, true, 0.5, STAKE_STRIKE",
    "true, false, false, 0.0, STAKE_STRIKE",
    "false, true, false, 0.5, TRADE",
    "false, false, false, 0.0, IDLE",
    "false, false, true, 0.0, IDLE",
    "false, false, true, 0.05, IDLE",
    "false, false, false, 0.3, WALK",
    "false, false, false, 0.55, WALK",
    "false, false, true, 0.3, RUN",
    "false, false, false, 0.8, RUN"
  })
  void picksTheClipForWhatTheHunterIsDoing(
      final boolean swinging,
      final boolean trading,
      final boolean sprinting,
      final float limbSwing,
      final HunterPose pose) {
    assertEquals(pose, HunterPose.of(swinging, trading, sprinting, limbSwing));
  }

  @ParameterizedTest(name = "{0} plays {1}")
  @CsvSource({"IDLE, idle", "WALK, walk", "RUN, run", "STAKE_STRIKE, stake_strike", "TRADE, trade"})
  void playsTheClipOfTheSameName(final HunterPose pose, final String clip) {
    assertEquals(clip, pose.animation());
  }
}
