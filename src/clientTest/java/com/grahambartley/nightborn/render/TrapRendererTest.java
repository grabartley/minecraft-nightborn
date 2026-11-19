package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TrapRendererTest {

  @ParameterizedTest(name = "vampire {0}, distance² {1}, seen within {2} -> shown {3}")
  @CsvSource({
    "false, 400, 2, true",
    "true, 4, 2, true",
    "true, 4.1, 2, false",
    "true, 36, 6, true",
    "true, 37, 6, false"
  })
  @DisplayName("a vampire sees a hunter's trap only from close by, and anyone else always does")
  void shown(
      final boolean vampire, final double distance, final float seenWithin, final boolean shown) {
    assertEquals(shown, TrapRenderer.shown(vampire, distance, seenWithin));
  }
}
