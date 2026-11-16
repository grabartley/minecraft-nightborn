package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HunterPoseTest {

  @ParameterizedTest(name = "swing {0}, trade {1}, sprint {2}, speed squared {3}: {4}")
  @CsvSource({
    "true, true, true, 0.5, STAKE_STRIKE",
    "false, true, false, 0.5, TRADE",
    "false, false, false, 0.0, IDLE",
    "false, false, true, 0.001, IDLE",
    "false, false, false, 0.02, WALK",
    "false, false, true, 0.02, RUN"
  })
  void picksTheClipForWhatTheHunterIsDoing(
      final boolean swinging,
      final boolean trading,
      final boolean sprinting,
      final double speedSquared,
      final HunterPose pose) {
    assertEquals(pose, HunterPose.of(swinging, trading, sprinting, speedSquared));
  }

  @ParameterizedTest(name = "{0} plays {1}")
  @CsvSource({"IDLE, idle", "WALK, walk", "RUN, run", "STAKE_STRIKE, stake_strike", "TRADE, trade"})
  void playsTheClipOfTheSameName(final HunterPose pose, final String clip) {
    assertEquals(clip, pose.animation());
  }
}
