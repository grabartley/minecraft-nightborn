package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

  @ParameterizedTest(name = "chance {0} kept as {1}, days {2} kept as {3}")
  @CsvSource({"-5, 0, 0, 1", "0, 0, 1, 1", "40, 40, 7, 7", "100, 100, 30, 30", "250, 100, 99, 30"})
  @DisplayName("the constructor clamps every chance and the sickness length into bounds")
  void clamps(final int chance, final int keptChance, final int days, final int keptDays) {
    final TurningSettings settings = new TurningSettings(chance, chance, chance, true, days);
    assertEquals(keptChance, settings.biteTurnChance());
    assertEquals(keptChance, settings.vampireMobTurnChance());
    assertEquals(keptChance, settings.vampireBatTurnChance());
    assertEquals(keptDays, settings.sicknessDays());
  }
}
