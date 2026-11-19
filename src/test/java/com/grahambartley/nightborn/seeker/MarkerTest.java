package com.grahambartley.nightborn.seeker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MarkerTest {

  @ParameterizedTest(name = "hunter {0}, ally {1}, own {2}, allied {3} -> {4}")
  @CsvSource({
    "true, false, 0.15, 0.1, 0.15",
    "false, true, 0.15, 0.1, 0.1",
    "false, false, 0.15, 0.1, 0",
    "true, false, 0, 0.1, 0"
  })
  @DisplayName("a marker's share follows who struck the blow")
  void share(
      final boolean byHunter,
      final boolean byAlly,
      final double own,
      final double allied,
      final double share) {
    assertEquals(share, new Marker(byHunter, byAlly, own, allied).share(), 1.0e-9);
  }
}
