package com.grahambartley.nightborn.blood;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PotionWardTest {

  @ParameterizedTest(name = "{0} ticks, infinite {1}, harmful {2}, resistance {3}: {4}")
  @CsvSource({
    "400, false, true, 0.5, 200",
    "400, false, true, 0.25, 300",
    "400, false, true, 0.0, 400",
    "400, false, false, 0.5, 400",
    "400, true, true, 0.5, 400",
    "400, false, true, 1.0, 1",
    "400, false, true, 3.0, 1",
    "1, false, true, 0.5, 1"
  })
  @DisplayName("potion resistance shortens only harmful effects with an end")
  void duration(
      final int duration,
      final boolean infinite,
      final boolean harmful,
      final double resistance,
      final int kept) {
    assertEquals(kept, PotionWard.duration(duration, infinite, harmful, resistance));
  }
}
