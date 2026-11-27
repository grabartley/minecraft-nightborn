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
    final FirstPersonSway sway =
        FirstPersonSway.of(Math.toRadians(raise), Math.toRadians(flare), 0.0);
    assertEquals(pitch, sway.pitchDegrees(), 1e-4);
    assertEquals(yaw, sway.yawDegrees(), 1e-4);
  }

  @ParameterizedTest(name = "{0} pixels forward: reach {1}")
  @CsvSource({"0, 0", "2, 0.1", "-1.5, -0.075", "5, 0.25", "12, 0.25", "-12, -0.25"})
  @DisplayName(
      "an arm pushed forward or drawn back in the clip moves the first person hand with it, up"
          + " to a limit")
  void reaches(final double forwardPixels, final float reach) {
    final FirstPersonSway sway = FirstPersonSway.of(Math.toRadians(80.0), 0.0, forwardPixels);
    assertEquals(reach, sway.reach(), 1e-6);
    assertEquals(0.0F, sway.pitchDegrees(), 1e-4);
  }

  @ParameterizedTest(name = "share {0}")
  @CsvSource({"0, 0, 0, 0", "0.5, 10, -5, 0.05", "1, 20, -10, 0.1"})
  @DisplayName("a sway scales down while the clip blends in")
  void scales(final double share, final float pitch, final float yaw, final float reach) {
    final FirstPersonSway sway = new FirstPersonSway(20.0F, -10.0F, 0.1F).scaled(share);
    assertEquals(pitch, sway.pitchDegrees(), 1e-4);
    assertEquals(yaw, sway.yawDegrees(), 1e-4);
    assertEquals(reach, sway.reach(), 1e-6);
  }

  @Test
  @DisplayName("no sway leaves the hand where vanilla holds it")
  void still() {
    assertTrue(FirstPersonSway.NONE.still());
    assertFalse(new FirstPersonSway(1.0F, 0.0F, 0.0F).still());
    assertFalse(new FirstPersonSway(0.0F, 0.0F, 0.1F).still());
  }

  @ParameterizedTest(name = "{0}s into a 0.6s clip: {1}")
  @CsvSource({
    "-0.1, 0.0",
    "-0.05, 0.5",
    "0.0, 1.0",
    "0.3, 1.0",
    "0.35, 1.0",
    "0.475, 0.5",
    "0.6, 0.0",
    "0.7, 0.0"
  })
  @DisplayName(
      "the hand eases in while the clip blends in and back to where vanilla holds it as the clip"
          + " settles")
  void weight(final double clipSeconds, final double expected) {
    assertEquals(expected, FirstPersonSway.weight(clipSeconds, 0.6, 0.1), 1e-9);
  }
}
