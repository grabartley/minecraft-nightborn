package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
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
    assertEquals(
        setting.min(), setting.get(NightbornConfig.DEFAULTS.with(setting, setting.min() - 1)));
    assertEquals(
        setting.max(), setting.get(NightbornConfig.DEFAULTS.with(setting, setting.max() + 1)));
  }
}
