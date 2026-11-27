package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class GuildSettingsTest {

  static Stream<Setting<?>> settings() {
    return GuildSettings.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(GuildSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  @ParameterizedTest(name = "oath {0}, wait {1}s, shelter {2}s is kept as {3}s and {4}s")
  @CsvSource({
    "true, 600, 3, 600, 3",
    "false, -5, -1, 0, 0",
    "true, 99999, 99, 7200, 30",
    "false, 0, 30, 0, 30"
  })
  void clampsToItsBounds(
      final boolean oath, final int wait, final int shelter, final int keptWait, final int kept) {
    final GuildSettings settings = new GuildSettings(oath, wait, shelter);
    assertEquals(oath, settings.riteNeedsOath());
    assertEquals(keptWait, settings.riteCooldownSeconds());
    assertEquals(kept, settings.riteShieldSeconds());
  }
}
