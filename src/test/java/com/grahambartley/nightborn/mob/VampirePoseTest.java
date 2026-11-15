package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VampirePoseTest {

  @ParameterizedTest(name = "downed {0}, attacking {1}, swing {2}: {3}")
  @CsvSource({
    "true, true, 0.9, DOWNED, downed",
    "false, true, 0.9, ATTACK, attack",
    "false, false, 0.9, RUN, run",
    "false, false, 0.3, WALK, walk",
    "false, false, 0.05, IDLE, idle"
  })
  @DisplayName("being downed wins over attacking, which wins over moving")
  void pose(
      final boolean downed,
      final boolean attacking,
      final float swing,
      final VampirePose pose,
      final String animation) {
    assertEquals(pose, VampirePose.of(downed, attacking, swing));
    assertEquals(animation, pose.animation());
  }
}
