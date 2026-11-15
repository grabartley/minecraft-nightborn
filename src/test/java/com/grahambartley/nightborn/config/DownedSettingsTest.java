package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class DownedSettingsTest {

  static Stream<Setting<?>> settings() {
    return DownedSettings.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("every setting in the section belongs to it and reads its default")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(DownedSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  @Test
  @DisplayName("the constructor clamps values into bounds")
  void clamps() {
    assertEquals(new DownedSettings(5, 5, 1, true), new DownedSettings(0, 0, 0, true));
    assertEquals(
        new DownedSettings(600, 600, 100, false), new DownedSettings(601, 601, 101, false));
  }
}
