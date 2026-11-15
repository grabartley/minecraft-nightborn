package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HuntingTest {

  @ParameterizedTest(name = "peaceful {0}, time {1}, open sky {2}: bat hunts {3}")
  @CsvSource({
    "false, 6000, true, false",
    "false, 6000, false, true",
    "false, 14000, true, true",
    "false, 14000, false, true",
    "true, 14000, true, false",
    "true, 6000, false, false"
  })
  @DisplayName("vampire bats hunt under cover, or in the open at night, never in peaceful")
  void batHunts(
      final boolean peaceful, final long time, final boolean openSky, final boolean hunts) {
    assertEquals(hunts, Hunting.batHunts(peaceful, false, time, openSky));
  }

  @ParameterizedTest(name = "hunting hours {0}, endangered {1}, provoked {2}: drops {3}")
  @CsvSource({
    "true, false, false, false",
    "true, false, true, false",
    "false, false, false, true",
    "false, false, true, false",
    "true, true, false, true",
    "false, true, true, true"
  })
  @DisplayName("a vampire drops its prey by day or at dawn, and its foe only to reach shade")
  void dropsTarget(
      final boolean hours, final boolean endangered, final boolean provoked, final boolean drops) {
    assertEquals(drops, Hunting.dropsTarget(hours, endangered, provoked));
  }
}
