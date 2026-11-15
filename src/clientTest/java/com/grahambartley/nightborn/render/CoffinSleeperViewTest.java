package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CoffinSleeperViewTest {

  @ParameterizedTest(name = "shut {0}, lid open {1}: hidden {2}")
  @CsvSource({"true, 0.0, true", "true, 0.3, false", "false, 0.0, false", "false, 1.0, false"})
  @DisplayName("a sleeper is hidden only once the lid has closed all the way over them")
  void hidesOnlyUnderAClosedLid(final boolean shut, final float openness, final boolean hidden) {
    assertEquals(hidden, CoffinSleeperView.hides(shut, openness));
  }

  @ParameterizedTest(name = "in a coffin {0}, lift {1}: {2}")
  @CsvSource({"true, 0.3, 0.0", "false, 0.3, 0.3", "false, 0.0, 0.0"})
  @DisplayName(
      "a sleeper in a coffin looks up from where they lie, and a bed sleeper keeps the lift")
  void dropsTheLiftInACoffin(final boolean inCoffin, final float lift, final float expected) {
    assertEquals(expected, CoffinSleeperView.cameraLift(inCoffin, lift));
  }
}
