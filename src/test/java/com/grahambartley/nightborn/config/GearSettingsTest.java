package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class GearSettingsTest {

  static Stream<Setting<?>> settings() {
    return GearSettings.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("every setting in the section belongs to it and reads its default")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(GearSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  @Test
  @DisplayName("the constructor clamps values into bounds")
  void clamps() {
    assertEquals(
        new GearSettings(0, 0, 0, 1, 0, 1, 1, 1), new GearSettings(-1, -1, -1, 0, -1, 0, 0, 0));
    assertEquals(
        new GearSettings(20, 40, 100, 120, 100, 60, 8, 60),
        new GearSettings(21, 41, 101, 121, 101, 61, 9, 61));
  }
}
