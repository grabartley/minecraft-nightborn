package com.grahambartley.nightborn.mob.household;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WalkTest {

  @ParameterizedTest(name = "{0} blocks squared away, reach {1}: {2}")
  @CsvSource({"0.0, 1.5, true", "2.25, 1.5, true", "2.26, 1.5, false", "16.0, 3.0, false"})
  @DisplayName("a vampire has arrived once it is within reach")
  void arrived(final double distanceSquared, final double reach, final boolean arrived) {
    assertEquals(arrived, Walk.arrived(distanceSquared, reach));
  }
}
