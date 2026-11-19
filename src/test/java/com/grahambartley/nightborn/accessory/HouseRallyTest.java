package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HouseRallyTest {

  @ParameterizedTest(name = "{0} blocks away, range {1}, boost {2}: {3}")
  @CsvSource({
    "0.0, 16.0, 1.1, 1.1",
    "16.0, 16.0, 1.1, 1.1",
    "16.1, 16.0, 1.1, 1.0",
    "4.0, 0.0, 1.1, 1.0",
    "4.0, 16.0, 1.0, 1.0",
    "4.0, 16.0, 0.5, 1.0"
  })
  @DisplayName("a member strikes harder only within the wearer's range, and never weaker")
  void rally(final double distance, final double range, final double boost, final double factor) {
    assertEquals(factor, HouseRally.rally(distance * distance, range, boost), 1.0E-9);
  }
}
