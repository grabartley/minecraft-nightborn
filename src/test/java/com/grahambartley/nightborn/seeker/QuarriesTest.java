package com.grahambartley.nightborn.seeker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class QuarriesTest {

  @ParameterizedTest(name = "veiled {0}, boss {1}, in sight {2} -> outlined {3}")
  @CsvSource({
    "false, false, false, true",
    "true, false, true, false",
    "false, true, true, true",
    "false, true, false, false",
    "true, true, true, false"
  })
  @DisplayName(
      "a quarry shows through walls unless veiled, and a boss only while the hunter can see it")
  void outlined(
      final boolean veiled, final boolean boss, final boolean inSight, final boolean shown) {
    assertEquals(shown, Quarries.outlined(veiled, boss, inSight));
  }
}
