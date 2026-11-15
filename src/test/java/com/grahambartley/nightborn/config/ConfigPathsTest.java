package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ConfigPathsTest {

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"world", "saves/My World", "/srv/minecraft/world"})
  void settingsLiveInTheWorldSave(final String root) {
    assertEquals(
        Path.of(root, "nightborn", "server-config.json"), ConfigPaths.serverConfig(Path.of(root)));
  }
}
