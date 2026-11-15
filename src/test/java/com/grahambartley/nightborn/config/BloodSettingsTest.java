package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class BloodSettingsTest {

  static Stream<Setting<?>> settings() {
    return BloodSettings.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("every setting in the section belongs to it and reads its default")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(BloodSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  @ParameterizedTest(name = "{0} defaults to {1}")
  @CsvSource({"restingDrainSeconds, 300"})
  @DisplayName("balance settings keep the default the PRD states")
  void keepsPrdDefault(final String key, final int expected) {
    assertEquals(
        expected,
        BloodSettings.SETTINGS.stream()
            .filter(setting -> setting.key().equals(key))
            .findFirst()
            .orElseThrow()
            .defaultValue());
  }

  @Test
  @DisplayName("the constructor clamps values into bounds")
  void clamps() {
    assertEquals(
        new BloodSettings(5, 0, 0, 0, 0, 5, 1, 0, 1, 1, 0, 0),
        new BloodSettings(0, -1, -1, -1, -1, 0, 0, -1, 0, 0, -1, -1));
    assertEquals(
        new BloodSettings(600, 500, 500, 500, 99, 1200, 100, 3600, 600, 100, 100, 40),
        new BloodSettings(9000, 900, 900, 900, 100, 9000, 101, 9000, 9000, 101, 101, 41));
  }
}
