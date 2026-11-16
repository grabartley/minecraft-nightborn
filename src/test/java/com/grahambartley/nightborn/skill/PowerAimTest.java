package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PowerAimTest {

  @Test
  @DisplayName("a missing power aims at nothing, with no gaze target and no place to step")
  void none() {
    final PowerAim aim = PowerAim.of(null, Optional.empty());
    assertSame(PowerAim.NONE, aim);
    assertNull(aim.gazeTarget());
    assertNull(aim.stepTo());
  }
}
