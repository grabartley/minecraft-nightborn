package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class ServantSettingsTest {

  static Stream<Setting<?>> settings() {
    return ServantSettings.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("every setting in the section belongs to it and reads its default")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(ServantSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  @ParameterizedTest(name = "{0}, {1}, {2}, {3} are kept as {4}, {5}, {6}, {7}")
  @CsvSource({
    "0, 0, 0, 0, 2, 32, 8, 4",
    "2, 32, 8, 4, 2, 32, 8, 4",
    "8, 256, 24, 16, 8, 256, 24, 16",
    "24, 1024, 32, 32, 24, 1024, 32, 32",
    "99, 9999, 99, 99, 24, 1024, 32, 32"
  })
  @DisplayName("the constructor clamps every day job range into bounds")
  void clamps(
      final int gather,
      final int scout,
      final int sight,
      final int watch,
      final int keptGather,
      final int keptScout,
      final int keptSight,
      final int keptWatch) {
    final ServantSettings settings = new ServantSettings(gather, scout, sight, watch);
    assertEquals(keptGather, settings.gatherRange());
    assertEquals(keptScout, settings.scoutRange());
    assertEquals(keptSight, settings.scoutSight());
    assertEquals(keptWatch, settings.watchRange());
  }
}
