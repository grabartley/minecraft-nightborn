package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class TurningSettingsTest {

  static Stream<Setting<?>> settings() {
    return TurningSettings.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("every setting in the section belongs to it and reads its default")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(TurningSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  @Test
  @DisplayName("the constructor clamps values into bounds")
  void clamps() {
    final TurningSettings low = new TurningSettings(-5, true, 0);
    assertEquals(0, low.biteTurnChance());
    assertEquals(1, low.sicknessDays());
    final TurningSettings high = new TurningSettings(250, false, 99);
    assertEquals(100, high.biteTurnChance());
    assertEquals(30, high.sicknessDays());
  }
}
