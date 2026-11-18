package com.grahambartley.nightborn.sight;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HeartbeatTest {

  @ParameterizedTest(name = "worth {0} of {1}, health {2}/{3}: weight {4}")
  @CsvSource({
    "100, 100, 20, 20, 1.0",
    "50, 100, 20, 20, 0.5",
    "100, 100, 10, 20, 0.5",
    "0, 100, 20, 20, 0.0",
    "100, 0, 20, 20, 0.0",
    "200, 100, 20, 20, 1.0"
  })
  @DisplayName("a heartbeat weighs the blood a creature is worth and how much of it is left")
  void weight(
      final int worth, final int most, final float health, final float max, final double weight) {
    assertEquals(weight, Heartbeat.weight(worth, most, health, max), 1.0E-9);
  }

  @ParameterizedTest(name = "weight {0}: colour {1}")
  @CsvSource({"0.0, 14726324", "1.0, 9044739", "-1.0, 14726324", "2.0, 9044739"})
  @DisplayName("a heartbeat runs from pale to deep red as the blood grows")
  void colour(final double weight, final int colour) {
    assertEquals(colour, Heartbeat.colour(weight));
  }
}
