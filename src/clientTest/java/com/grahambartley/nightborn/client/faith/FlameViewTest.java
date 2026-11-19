package com.grahambartley.nightborn.client.faith;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FlameViewTest {

  @ParameterizedTest(name = "first person {0}, holds a flame {1}, distance squared {2}: hidden {3}")
  @CsvSource({
    "true, true, 0.5, true",
    "true, true, 2.5, true",
    "true, true, 2.6, false",
    "false, true, 0.5, false",
    "true, false, 0.5, false"
  })
  @DisplayName("your own blade's flames stay out of your face in first person")
  void hides(
      final boolean firstPerson,
      final boolean holds,
      final double distanceSquared,
      final boolean hidden) {
    assertEquals(hidden, FlameView.hides(firstPerson, holds, distanceSquared));
  }
}
