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

  @ParameterizedTest(
      name = "percent {0} kept as {1}, per thousand {2} kept as {3}, days {4} kept as {5}")
  @CsvSource({
    "-5, 0, -5, 0, 0, 1",
    "0, 0, 0, 0, 1, 1",
    "40, 40, 1, 1, 7, 7",
    "100, 100, 1000, 1000, 30, 30",
    "250, 100, 4000, 1000, 99, 30"
  })
  @DisplayName("the constructor clamps every chance, cost and length into bounds")
  void clamps(
      final int percent,
      final int keptPercent,
      final int perThousand,
      final int keptPerThousand,
      final int days,
      final int keptDays) {
    final TurningSettings settings =
        new TurningSettings(percent, perThousand, percent, true, days, days);
    assertEquals(keptPercent, settings.biteTurnChance());
    assertEquals(keptPerThousand, settings.batTurnChancePerThousand());
    assertEquals(keptPercent, settings.turningBiteBloodCost());
    assertEquals(keptDays, settings.sicknessDays());
    assertEquals(Math.max(1, Math.min(60, days)), settings.oldBloodRefillDays());
  }
}
