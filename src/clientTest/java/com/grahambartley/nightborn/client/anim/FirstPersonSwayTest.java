package com.grahambartley.nightborn.client.anim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FirstPersonSwayTest {

  @ParameterizedTest(name = "raise {0}, flare {1}: pitch {2}, yaw {3}")
  @CsvSource({
    "80, 0, 0, 0",
    "140, 0, 30, 0",
    "40, 0, -20, 0",
    "80, 30, 0, -15",
    "80, -30, 0, 15",
    "180, 0, 45, 0",
    "0, 0, -40, 0",
    "-30, -120, -45, 45"
  })
  @DisplayName(
      "half of how far an arm is raised past reaching forward, and of its flare, reaches the"
          + " first person hand, up to a limit")
  void follows(final double raise, final double flare, final float pitch, final float yaw) {
    final FirstPersonSway sway = FirstPersonSway.of(Math.toRadians(raise), Math.toRadians(flare));
    assertEquals(pitch, sway.pitchDegrees(), 1e-4);
    assertEquals(yaw, sway.yawDegrees(), 1e-4);
  }

  @ParameterizedTest(name = "share {0}")
  @CsvSource({"0, 0, 0", "0.5, 10, -5", "1, 20, -10"})
  @DisplayName("a sway scales down while the clip blends in")
  void scales(final double share, final float pitch, final float yaw) {
    final FirstPersonSway sway = new FirstPersonSway(20.0F, -10.0F).scaled(share);
    assertEquals(pitch, sway.pitchDegrees(), 1e-4);
    assertEquals(yaw, sway.yawDegrees(), 1e-4);
  }

  @Test
  @DisplayName("no sway leaves the hand where vanilla holds it")
  void still() {
    assertTrue(FirstPersonSway.NONE.still());
    assertFalse(new FirstPersonSway(1.0F, 0.0F).still());
  }
}
