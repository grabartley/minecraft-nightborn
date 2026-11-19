package com.grahambartley.nightborn.inquisitor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HawkFlightTest {

  @ParameterizedTest(name = "rising {0}: {1}")
  @CsvSource({"0.3, FLY", "0.021, FLY", "0.02, GLIDE", "0, GLIDE", "-0.4, GLIDE"})
  @DisplayName("the hawk beats its wings to climb and glides level or falling")
  void of(final double rise, final HawkFlight flight) {
    assertEquals(flight, HawkFlight.of(rise));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"FLY, fly", "GLIDE, glide"})
  @DisplayName("each flight loops its own clip")
  void clip(final HawkFlight flight, final String clip) {
    assertEquals(clip, flight.clip().getAnimationStages().get(0).animationName());
  }
}
