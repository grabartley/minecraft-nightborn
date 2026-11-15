package com.grahambartley.nightborn.sickness;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SicknessStageTest {

  @ParameterizedTest(name = "{0} ticks left of {1} days: day {2}, {3} left")
  @CsvSource({
    "72000, 3, 1, 3, true, false, false",
    "48001, 3, 1, 3, true, false, false",
    "48000, 3, 2, 2, true, true, false",
    "24001, 3, 2, 2, true, true, false",
    "24000, 3, 3, 1, true, true, true",
    "1, 3, 3, 1, true, true, true",
    "24000, 1, 1, 1, true, false, false",
    "240000, 10, 1, 10, true, false, false",
    "1, 10, 10, 1, true, true, true"
  })
  @DisplayName("the sickness worsens each day: hunger, then Weakness, then daylight slowness")
  void stages(
      final int ticksLeft,
      final int days,
      final int day,
      final int daysLeft,
      final boolean hunger,
      final boolean weakness,
      final boolean slowness) {
    assertEquals(
        new SicknessStage(day, daysLeft, hunger, weakness, slowness),
        SicknessStage.of(ticksLeft, days));
  }

  @ParameterizedTest(name = "{0} days")
  @CsvSource({"1, 24000", "3, 72000", "0, 24000"})
  void totals(final int days, final int ticks) {
    assertEquals(ticks, SicknessStage.totalTicks(days));
  }
}
