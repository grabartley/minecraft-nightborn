package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class CoatingSettingsTest {

  static Stream<Setting<?>> settings() {
    return CoatingSettings.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("every setting in the section belongs to it and reads its default")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(CoatingSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  @Test
  @DisplayName("the constructor clamps values into bounds")
  void clamps() {
    assertEquals(
        new CoatingSettings(0, 1, 10, 0, 1, 0, 1, 10, 0, 1),
        new CoatingSettings(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1));
    assertEquals(
        new CoatingSettings(19, 500, 3600, 100, 60, 19, 500, 3600, 100, 60),
        new CoatingSettings(99, 999, 9999, 999, 999, 99, 999, 9999, 999, 999));
  }
}
