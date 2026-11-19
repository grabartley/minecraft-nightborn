package com.grahambartley.nightborn.guild.veteran;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GroundingTest {

  @ParameterizedTest(name = "now {0}, until {1}: held {2}")
  @CsvSource({"10, 70, true", "69, 70, true", "70, 70, false", "80, 70, false", "10, , false"})
  void holdsAVampireDownUntilItRunsOut(final long now, final Long until, final boolean held) {
    assertEquals(held, Grounding.held(now, until));
  }

  @ParameterizedTest(name = "rising {0}, held {1}: {2}")
  @CsvSource({"0.4, true, 0.0", "-0.2, true, -0.2", "0.4, false, 0.4", "0.0, true, 0.0"})
  void letsAHeldVampireFallButNeverRise(
      final double velocity, final boolean held, final double after) {
    assertEquals(after, Grounding.rise(velocity, held), 1.0E-9);
  }
}
