package com.grahambartley.nightborn.sickness;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SicknessStageTest {

  @ParameterizedTest(name = "{0} ticks run of {1} days: day {2}, {3} left")
  @CsvSource({
    "0, 3, 1, 3, true, false, false",
    "23999, 3, 1, 3, true, false, false",
    "24000, 3, 2, 2, true, true, false",
    "47999, 3, 2, 2, true, true, false",
    "48000, 3, 3, 1, true, true, true",
    "71999, 3, 3, 1, true, true, true",
    "0, 1, 1, 1, true, false, false",
    "23999, 1, 1, 1, true, false, false",
    "48000, 1, 1, 0, true, false, false",
    "0, 10, 1, 10, true, false, false",
    "239999, 10, 10, 1, true, true, true"
  })
  @DisplayName("the sickness worsens each day: hunger, then Weakness, then daylight slowness")
  void stages(
      final int ticksRun,
      final int days,
      final int day,
      final int daysLeft,
      final boolean hunger,
      final boolean weakness,
      final boolean slowness) {
    assertEquals(
        new SicknessStage(day, daysLeft, hunger, weakness, slowness),
        SicknessStage.of(ticksRun, days));
  }

  @ParameterizedTest(name = "{0} ticks run of {1} days leaves {2}")
  @CsvSource({
    "0, 3, 72000",
    "30000, 3, 42000",
    "30000, 1, 0",
    "23999, 1, 1",
    "-5, 1, 24000",
    "2147483647, 30, 0"
  })
  @DisplayName("time left follows the current length, so a shorter length applies at once")
  void ticksLeft(final int ticksRun, final int days, final int left) {
    assertEquals(left, SicknessStage.ticksLeft(ticksRun, days));
  }

  @ParameterizedTest(name = "{0} days")
  @CsvSource({"1, 24000", "3, 72000", "0, 24000"})
  void totals(final int days, final int ticks) {
    assertEquals(ticks, SicknessStage.totalTicks(days));
  }
}
