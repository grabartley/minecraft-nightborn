package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DamageTakenTest {

  @ParameterizedTest(name = "{0} cut to {1}: {2}")
  @CsvSource({"10, 5, 5", "10, 1, 2", "10, 20, 20", "10, 10, 10"})
  @DisplayName("no mix of reductions takes more than 80% off a blow")
  void floors(final float original, final float reduced, final float taken) {
    assertEquals(taken, DamageTaken.floored(original, reduced), 1.0E-6);
  }
}
