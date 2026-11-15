package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ConfigFileTest {

  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-11-15T10:20:30Z"), ZoneOffset.UTC);

  @TempDir Path world;

  @Test
  @DisplayName("a missing file gives defaults and writes them out")
  void missingWritesDefaults() {
    final Path path = ConfigPaths.serverConfig(world);
    final NightbornConfig loaded = new ConfigFile(path, CLOCK).loadOrCreate();
    assertEquals(NightbornConfig.DEFAULTS, loaded);
    assertTrue(Files.exists(path));
    assertEquals(NightbornConfig.DEFAULTS, new ConfigFile(path, CLOCK).load());
  }

  @Test
  @DisplayName("saved settings load back")
  void saveThenLoad() {
    final Path path = ConfigPaths.serverConfig(world);
    final NightbornConfig config = NightbornConfig.DEFAULTS.with(SunSettings.SUN_DAMAGE, 11);
    assertTrue(new ConfigFile(path, CLOCK).save(config));
    assertEquals(config, new ConfigFile(path, CLOCK).loadOrCreate());
    assertFalse(Files.exists(path.resolveSibling("server-config.json.tmp")));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"{ not json", "[1, 2]", "\"text\"", "42"})
  @DisplayName("a broken file gives defaults and is kept, not overwritten")
  void brokenFileKept(final String contents) throws IOException {
    final Path path = ConfigPaths.serverConfig(world);
    Files.createDirectories(path.getParent());
    Files.writeString(path, contents, StandardCharsets.UTF_8);
    final ConfigFile file = new ConfigFile(path, CLOCK);
    assertEquals(NightbornConfig.DEFAULTS, file.load());
    final Path kept = path.resolveSibling("server-config.broken.2026-11-15T10-20-30Z.json");
    assertTrue(Files.exists(kept));
    assertEquals(contents, Files.readString(kept, StandardCharsets.UTF_8));
  }

  @Test
  @DisplayName("a broken file is never replaced by loadOrCreate's defaults")
  void loadOrCreateKeepsBrokenFile() throws IOException {
    final Path path = ConfigPaths.serverConfig(world);
    Files.createDirectories(path.getParent());
    Files.writeString(path, "{ broken", StandardCharsets.UTF_8);
    new ConfigFile(path, CLOCK).loadOrCreate();
    try (Stream<Path> files = Files.list(path.getParent())) {
      assertEquals(1, files.filter(p -> p.getFileName().toString().contains("broken")).count());
    }
  }
}
