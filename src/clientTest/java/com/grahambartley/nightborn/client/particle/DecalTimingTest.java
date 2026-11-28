package com.grahambartley.nightborn.client.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DecalTimingTest {

  @ParameterizedTest(name = "gore share {0}, pool {1}, roll {2}: {3} ticks")
  @CsvSource({
    "1.0, false, 0.0, 5400",
    "1.0, false, 1.0, 7200",
    "1.0, false, 0.5, 6300",
    "1.0, true, 0.0, 6210",
    "1.0, true, 1.0, 8280",
    "0.5, false, 0.0, 2700",
    "0.5, true, 1.0, 4140",
    "0.0, false, 0.5, 0",
    "1.0, false, -1.0, 5400",
    "1.0, false, 2.0, 7200"
  })
  @DisplayName("blood lasts four and a half to six minutes, pools longer, reduced gore half")
  void bloodLife(final float share, final boolean pool, final float roll, final int expected) {
    assertEquals(expected, DecalTiming.bloodLife(share, pool, roll));
  }

  @ParameterizedTest(name = "roll {0}: {1} ticks")
  @CsvSource({"0.0, 70", "0.5, 90", "1.0, 110"})
  @DisplayName("vomit keeps its own short life")
  void vomitLife(final float roll, final int expected) {
    assertEquals(expected, DecalTiming.vomitLife(roll));
  }
}
