package com.grahambartley.nightborn.sickness;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SicknessRulesTest {

  @ParameterizedTest(name = "curing food {0}, Weakness {1}, sick {2}: cures {3}")
  @CsvSource({
    "true, true, true, true",
    "true, false, true, false",
    "false, true, true, false",
    "true, true, false, false"
  })
  @DisplayName("a golden apple cures only under Weakness and only while sick")
  void cures(final boolean food, final boolean weak, final boolean sick, final boolean cures) {
    assertEquals(cures, SicknessRules.cures(food, weak, sick));
  }

  @ParameterizedTest(name = "{0} -> {1} days at age {2}: {3}")
  @CsvSource({
    "3, 2, 17, true",
    "3, 3, 17, false",
    "3, 3, 0, true",
    "2, 2, 6000, true",
    "2, 2, 6001, false"
  })
  @DisplayName("the days left are shown when a day passes and every five minutes")
  void reminds(final int before, final int now, final int age, final boolean reminds) {
    assertEquals(reminds, SicknessRules.remindsOfDaysLeft(before, now, age));
  }

  @ParameterizedTest(name = "shown {0} at {1} for {2}, now day {3} with {4}: refresh {5}")
  @CsvSource({
    "false, 0, 0, 0, 72000, true",
    "true, 0, 72000, 0, 71990, false",
    "true, 0, 72000, 0, 71959, true",
    "true, 0, 48000, 1, 48000, true"
  })
  @DisplayName("the shown effect follows the real sickness, so milk cannot hide it for long")
  void refreshes(
      final boolean shown,
      final int amplifier,
      final int duration,
      final int day,
      final int left,
      final boolean refresh) {
    assertEquals(refresh, SicknessRules.refreshesDisplay(shown, amplifier, duration, day, left));
  }
}
