package com.grahambartley.nightborn.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class WeakenedSignRendererTest {

  static Stream<Arguments> icons() {
    return Stream.of(
        Arguments.of("hud sprite", WeakenedSignRenderer.SPRITE, "nightborn:hud/weakened"),
        Arguments.of(
            "world sign texture",
            WeakenedSignRenderer.TEXTURE,
            "nightborn:textures/gui/sprites/hud/weakened.png"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("icons")
  @DisplayName("the HUD and the sign above the head both draw the mod's own weakened icon")
  void drawsOwnIcon(final String where, final Identifier id, final String expected) {
    assertEquals(expected, id.toString());
  }

  @Test
  @DisplayName("the weakened icon both of them draw ships with the mod")
  void iconShips() {
    final Identifier texture = WeakenedSignRenderer.TEXTURE;
    assertTrue(
        Files.exists(
            Path.of("src/main/resources/assets", texture.getNamespace(), texture.getPath())),
        texture.toString());
  }
}
