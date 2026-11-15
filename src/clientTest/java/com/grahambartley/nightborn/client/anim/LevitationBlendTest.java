package com.grahambartley.nightborn.client.anim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.mob.VampirePose;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LevitationBlendTest {

  @ParameterizedTest(name = "speed {0}, sprinting {1}")
  @CsvSource({"0, false, 0, 0", "0.3, false, 1, 0", "0.3, true, 1, 1", "0.0, true, 0, 0"})
  @DisplayName("standing still floats, moving leans, and only sprinting spreads the arms")
  void targets(
      final double speed, final boolean sprinting, final double move, final double sprint) {
    final LevitationBlend blend = LevitationBlend.target(speed, sprinting);
    assertEquals(move, blend.move(), 1.0E-9);
    assertEquals(sprint, blend.sprint(), 1.0E-9);
  }

  @Test
  @DisplayName("the arms spread more the faster a sprint goes")
  void spreadGrowsWithSpeed() {
    final double slow = LevitationBlend.target(LevitationBlend.SPRINT_FROM + 0.02, true).sprint();
    final double fast =
        LevitationBlend.target(VampirePose.LEVITATE_SPRINT_SPEED - 0.01, true).sprint();
    assertTrue(slow > 0.0 && slow < fast && fast < 1.0);
  }

  @ParameterizedTest(name = "share {0}")
  @CsvSource({"0, 0", "0.5, 0.5", "1, 1"})
  @DisplayName("easing moves part of the way to the target")
  void approaches(final double share, final double move) {
    assertEquals(
        move, LevitationBlend.STILL.approach(new LevitationBlend(1, 0), share).move(), 1.0E-9);
  }
}
