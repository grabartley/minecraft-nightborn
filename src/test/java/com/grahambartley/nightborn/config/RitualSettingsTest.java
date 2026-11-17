package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class RitualSettingsTest {

  static Stream<Setting<?>> settings() {
    return RitualSettings.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("every setting in the section belongs to it and reads its default")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(RitualSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  @Test
  @DisplayName("the constructor clamps values into bounds")
  void clamps() {
    assertEquals(
        new RitualSettings(20, 0, 0, 8, 8, 4, 20, 5, 1, 0, 0, 0),
        new RitualSettings(19, -1, -1, 7, 7, 3, 19, 4, 0, -1, -1, -1));
    assertEquals(
        new RitualSettings(1200, 50000, 500, 128, 48, 32, 1200, 120, 100, 100, 100, 20),
        new RitualSettings(1201, 50001, 501, 129, 49, 33, 1201, 121, 101, 101, 101, 21));
  }
}
