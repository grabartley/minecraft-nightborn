package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class SunSettingsTest {

  static Stream<Setting<?>> settings() {
    return SunSettings.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("every setting in the section belongs to it and reads its default")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(SunSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  @ParameterizedTest(name = "sun damage {0} and parasol {1} s become {2} and {3}")
  @CsvSource({"0, 5, 1, 10", "41, 4000, 40, 3600", "2, 180, 2, 180"})
  @DisplayName("the constructor clamps values into bounds")
  void clamps(final int damage, final int parasol, final int keptDamage, final int keptParasol) {
    final SunSettings settings = new SunSettings(damage, parasol);
    assertEquals(keptDamage, settings.sunDamage());
    assertEquals(keptParasol, settings.parasolSeconds());
  }

  @ParameterizedTest(name = "{0} defaults to {1}")
  @CsvSource({"sunDamage, 2", "parasolSeconds, 180"})
  @DisplayName("the locked defaults hold")
  void lockedDefaults(final String key, final int value) {
    assertEquals(
        value,
        SunSettings.SETTINGS.stream()
            .filter(setting -> setting.key().equals(key))
            .findFirst()
            .orElseThrow()
            .defaultValue());
  }
}
