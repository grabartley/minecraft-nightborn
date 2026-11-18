package com.grahambartley.nightborn.blood;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PreySenseTest {

  @ParameterizedTest(name = "sense {0}, distance squared {1}, living {2}, self {3}: {4}")
  @CsvSource({
    "12, 144, true, false, true",
    "12, 145, true, false, false",
    "12, 0, true, true, false",
    "12, 4, false, false, false",
    "0, 1, true, false, false",
    "6, 36, true, false, true",
    "6, 37, true, false, false"
  })
  @DisplayName("prey sense shows living things within its range, never the vampire itself")
  void reveals(
      final double sense,
      final double distanceSquared,
      final boolean living,
      final boolean self,
      final boolean shown) {
    assertEquals(shown, PreySense.reveals(sense, distanceSquared, living, self));
  }
}
