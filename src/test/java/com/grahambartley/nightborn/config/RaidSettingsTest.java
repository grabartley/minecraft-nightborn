package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class RaidSettingsTest {

  static Stream<Setting<?>> settings() {
    return RaidSettings.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("every setting in the section belongs to it and reads its default")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(RaidSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  @ParameterizedTest(name = "wake {0}, fade {1}, watch {2}, spread {3}")
  @CsvSource({
    "10, 1, 3, 64, 10, 1, 3, 64",
    "0, -1, -1, -5, 1, 0, 0, 0",
    "5000, 500, 500, 9000, 1000, 100, 100, 512"
  })
  @DisplayName("each value is clamped into range on load")
  void clamps(
      final int wake,
      final int fade,
      final int watch,
      final int spread,
      final int keptWake,
      final int keptFade,
      final int keptWatch,
      final int keptSpread) {
    final RaidSettings settings = new RaidSettings(wake, fade, watch, spread);
    assertEquals(keptWake, settings.wakeNoise());
    assertEquals(keptFade, settings.noiseFade());
    assertEquals(keptWatch, settings.watchDays());
    assertEquals(keptSpread, settings.contractMapSpread());
  }
}
