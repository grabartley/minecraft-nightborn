package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class DismembermentSettingsTest {

  static Stream<Setting<?>> settings() {
    return DismembermentSettings.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("every setting in the section belongs to it and reads its default")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(DismembermentSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  @ParameterizedTest(name = "{0}/{1}/{2}/{3} kept as {4}/{5}/{6}/{7}")
  @CsvSource({
    "25, 20, 50, 35, 25, 20, 50, 35",
    "-5, 0, 0, -1, 0, 1, 1, 0",
    "150, 200, 101, 120, 100, 100, 100, 100"
  })
  @DisplayName("the constructor clamps every share to its bounds")
  void clamps(
      final int chance,
      final int strong,
      final int massive,
      final int hurt,
      final int keptChance,
      final int keptStrong,
      final int keptMassive,
      final int keptHurt) {
    final DismembermentSettings settings =
        new DismembermentSettings(true, true, chance, strong, massive, hurt);
    assertEquals(keptChance, settings.severChance());
    assertEquals(keptStrong, settings.strongBlow());
    assertEquals(keptMassive, settings.massiveBlow());
    assertEquals(keptHurt, settings.badlyHurt());
  }
}
