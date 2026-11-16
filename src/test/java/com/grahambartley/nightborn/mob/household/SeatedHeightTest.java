package com.grahambartley.nightborn.mob.household;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SeatedHeightTest {

  @ParameterizedTest(name = "{0} tall at scale {1} sinks {2} into the seat")
  @CsvSource({"2.0, 1.0, 0.7", "1.5, 1.0, 0.5125", "1.75, 1.0, 0.60625", "2.0, 0.5, 0.325"})
  @DisplayName("a seated vampire's hips rest on the seat whatever its size")
  void lift(final float height, final float scale, final double lift) {
    assertEquals(lift, SeatedHeight.lift(height, scale), 1.0E-6);
  }
}
