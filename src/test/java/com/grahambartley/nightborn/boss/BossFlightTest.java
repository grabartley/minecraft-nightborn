package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BossFlightTest {

  @ParameterizedTest(name = "airborne {0}, weightless {1}, a bat {2}: grounded {3}")
  @CsvSource({
    "false, true, false, true",
    "false, false, false, false",
    "true, true, false, false",
    "false, true, true, false"
  })
  @DisplayName(
      "a boss left weightless when it should not be flying is put back on the ground, whatever it"
          + " remembers")
  void grounds(
      final boolean airborne, final boolean weightless, final boolean bat, final boolean grounds) {
    assertEquals(grounds, BossFlight.grounds(airborne, weightless, bat));
  }
}
