package com.grahambartley.nightborn.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SicknessVignetteTest {

  @ParameterizedTest(name = "day index {0}: {1}")
  @CsvSource({"-1, 0.25", "0, 0.25", "1, 0.45", "2, 0.65", "3, 0.85", "20, 0.85"})
  @DisplayName("the red edge on screen grows as the days pass")
  void grows(final int dayIndex, final float alpha) {
    assertEquals(alpha, SicknessVignette.alpha(dayIndex), 0.0001F);
  }
}
