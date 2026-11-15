package com.grahambartley.nightborn.client.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.config.ConfigPaths;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class ClientStateServiceTest {

  @TempDir Path config;

  @Test
  @DisplayName("a missing file gives defaults and writes them out")
  void missingWritesDefaults() {
    ClientStateService.load(config);
    assertEquals(ClientState.DEFAULTS, ClientStateService.current());
    assertTrue(Files.exists(ConfigPaths.clientState(config)));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = GoreLevel.class,
      names = {"REDUCED", "OFF"})
  @DisplayName("a changed level is saved on the device and loads back")
  void changeIsSaved(final GoreLevel level) {
    ClientStateService.load(config);
    assertTrue(ClientStateService.update(ClientState.DEFAULTS.withGore(level)));
    ClientStateService.load(config);
    assertEquals(level, ClientStateService.current().gore());
  }

  @Test
  @DisplayName("saving an unchanged state writes nothing")
  void unchangedIsNotSaved() {
    ClientStateService.load(config);
    assertFalse(ClientStateService.update(ClientState.DEFAULTS));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"{ not json", "[1]", "\"off\""})
  @DisplayName("a broken file gives defaults and is kept beside it")
  void brokenFileKept(final String contents) throws IOException {
    final Path file = ConfigPaths.clientState(config);
    Files.createDirectories(file.getParent());
    Files.writeString(file, contents, StandardCharsets.UTF_8);
    ClientStateService.load(config);
    assertEquals(ClientState.DEFAULTS, ClientStateService.current());
    try (Stream<Path> files = Files.list(file.getParent())) {
      assertEquals(
          1, files.filter(path -> path.getFileName().toString().contains("broken")).count());
    }
  }
}
