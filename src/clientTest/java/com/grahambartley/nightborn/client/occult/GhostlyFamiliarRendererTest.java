package com.grahambartley.nightborn.client.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GhostlyFamiliarRendererTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource({"model, nightborn:ghostly_familiar", "shadow, 0.0"})
  @DisplayName("a ghostly familiar draws its own rig and casts no shadow")
  void drawsAGhost(final String part, final String expected) {
    final String actual =
        switch (part) {
          case "model" -> GhostlyFamiliarRenderer.MODEL.toString();
          default -> Float.toString(GhostlyFamiliarRenderer.SHADOW_RADIUS);
        };
    assertEquals(expected, actual);
  }
}
