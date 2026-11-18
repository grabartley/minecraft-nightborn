package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DeedFactsTest {

  @ParameterizedTest(name = "{0} blocks away reads {1}")
  @CsvSource({"0.0, 0", "63.9, 63", "64.0, 64", "-2.0, 0", "5000.0, 1000"})
  void roundsTheNearestPlayerDownAndCapsIt(final double distance, final int nearest) {
    assertEquals(nearest, DeedFacts.nearest(distance));
  }
}
