package com.grahambartley.nightborn.client.faith;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FlameViewTest {

  @ParameterizedTest(name = "first person {0}, distance squared {1}: hidden {2}")
  @CsvSource({"true, 0.5, true", "true, 2.5, true", "true, 2.6, false", "false, 0.5, false"})
  @DisplayName("your own blade's flames stay out of your face in first person")
  void hides(final boolean firstPerson, final double distanceSquared, final boolean hidden) {
    assertEquals(hidden, FlameView.hides(firstPerson, distanceSquared));
  }
}
