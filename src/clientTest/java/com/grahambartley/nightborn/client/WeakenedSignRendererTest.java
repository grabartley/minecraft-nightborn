package com.grahambartley.nightborn.client;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class WeakenedSignRendererTest {

  static Stream<Arguments> icons() {
    return Stream.of(
        Arguments.of(
            "hud sprite", "textures/gui/sprites/" + WeakenedSignRenderer.SPRITE.getPath() + ".png"),
        Arguments.of("world sign texture", WeakenedSignRenderer.TEXTURE.getPath()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("icons")
  @DisplayName("the icon the HUD and the sign above the head draw ships with the mod")
  void iconShips(final String where, final String path) {
    assertTrue(Files.exists(Path.of("src/main/resources/assets/nightborn", path)), where);
  }
}
