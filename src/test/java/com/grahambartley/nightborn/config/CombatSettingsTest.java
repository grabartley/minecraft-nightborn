package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class CombatSettingsTest {

  static Stream<IntSetting> settings() {
    return CombatSettings.SETTINGS.stream().map(IntSetting.class::cast);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("every setting in the section belongs to it and reads its default")
  void ownsItsSettings(final IntSetting setting) {
    assertEquals(CombatSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("every setting is clamped into its bounds")
  void clamps(final IntSetting setting) {
    final NightbornConfig open =
        NightbornConfig.DEFAULTS
            .with(CombatSettings.LEAP_MIN_RANGE, 0)
            .with(CombatSettings.DASH_MIN_RANGE, 0);
    assertEquals(setting.min(), setting.get(open.with(setting, setting.min() - 1)));
    assertEquals(setting.max(), setting.get(open.with(setting, setting.max() + 1)));
  }

  @ParameterizedTest(name = "range {0} to {1} keeps {0} to {2}")
  @CsvSource({"3, 7, 7", "10, 4, 10", "16, 1, 16"})
  @DisplayName("a move's longest range is never below its shortest")
  void rangesStayInOrder(final int min, final int max, final int kept) {
    final CombatSettings settings =
        NightbornConfig.DEFAULTS
            .with(CombatSettings.LEAP_MIN_RANGE, min)
            .with(CombatSettings.LEAP_MAX_RANGE, max)
            .with(CombatSettings.DASH_MIN_RANGE, min)
            .with(CombatSettings.DASH_MAX_RANGE, max)
            .combat();
    assertEquals(kept, settings.leapMaxRange());
    assertEquals(kept, settings.dashMaxRange());
  }
}
