package com.grahambartley.nightborn.batform;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BatEffortTest {

  @ParameterizedTest(name = "bat {0}, resting {1}, sprinting {2} -> {3}")
  @CsvSource({
    "false, false, false, NONE",
    "false, true, true, NONE",
    "true, true, false, RESTING",
    "true, true, true, RESTING",
    "true, false, false, FLYING",
    "true, false, true, SPRINTING"
  })
  @DisplayName("a landed or hanging bat rests, and a flying one works harder at a sprint")
  void of(
      final boolean bat, final boolean resting, final boolean sprinting, final BatEffort expected) {
    assertEquals(expected, BatEffort.of(bat, resting, sprinting));
  }
}
