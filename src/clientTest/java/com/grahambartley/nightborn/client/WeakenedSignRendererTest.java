package com.grahambartley.nightborn.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class WeakenedSignRendererTest {

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = "textures/gui/sprites/hud/weakened.png")
  @DisplayName("the sign above the head draws the shipped weakened icon")
  void drawsShippedIcon(final String path) {
    assertEquals("nightborn", WeakenedSignRenderer.TEXTURE.getNamespace());
    assertEquals(path, WeakenedSignRenderer.TEXTURE.getPath());
    assertTrue(Files.exists(Path.of("src/main/resources/assets/nightborn", path)), path);
  }
}
