package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DaylightHealingTest {

  @ParameterizedTest(name = "age {0}, every {1} ticks, hurt {2}: heals {3}")
  @CsvSource({
    "200, 200, true, true",
    "400, 200, true, true",
    "199, 200, true, false",
    "200, 200, false, false",
    "200, 0, true, false",
    "0, 200, true, false"
  })
  @DisplayName("a hurt wearer heals once each interval")
  void heals(final int age, final int interval, final boolean hurt, final boolean heals) {
    assertEquals(heals, DaylightHealing.heals(age, interval, hurt));
  }
}
