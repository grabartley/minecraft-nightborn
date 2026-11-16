package com.grahambartley.nightborn.client.thrall;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ThrallRendererTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "base, minecraft:textures/entity/villager/villager.png",
    "scale, 0.9375",
    "shadow, 0.5"
  })
  @DisplayName("a thrall is drawn on the villager's own body, size and shadow")
  void drawsAVillager(final String part, final String expected) {
    final String actual =
        switch (part) {
          case "base" -> ThrallRenderer.BASE.toString();
          case "scale" -> Float.toString(ThrallRenderer.SCALE);
          default -> Float.toString(ThrallRenderer.SHADOW);
        };
    assertEquals(expected, actual);
  }
}
