package com.grahambartley.nightborn.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EyeGlowTest {

  @ParameterizedTest(name = "feeding {0}, fed {1} ticks ago, health {2}/40: {3}")
  @CsvSource({
    "true, -1, 40, true",
    "false, 0, 40, true",
    "false, 99, 40, true",
    "false, 100, 40, false",
    "false, -1, 40, false",
    "false, -1, 11.9, true",
    "false, -1, 12, false",
    "false, 5000, 30, false"
  })
  @DisplayName("eyes glow while feeding, for five seconds after, and below 30% health")
  void glowing(
      final boolean feeding, final long sinceFed, final float health, final boolean glows) {
    assertEquals(glows, EyeGlow.glowing(feeding, sinceFed, health, 40.0F));
  }
}
