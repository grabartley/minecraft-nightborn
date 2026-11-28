package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WaresWatchTest {

  @ParameterizedTest(name = "alive {0}, buyer {1}, vampire {2}, fighting {3}, distance² {4} -> {5}")
  @CsvSource({
    "true, true, false, false, 4.0, false",
    "true, true, false, false, 16.0, false",
    "true, true, false, false, 16.1, true",
    "false, true, false, false, 1.0, true",
    "true, false, false, false, 1.0, true",
    "true, true, true, false, 1.0, true",
    "true, true, false, true, 1.0, true"
  })
  @DisplayName(
      "a hunter's trade closes if either side is gone, a fight starts, the buyer turns or walks off")
  void closes(
      final boolean alive,
      final boolean buyer,
      final boolean vampire,
      final boolean fighting,
      final double distance,
      final boolean closes) {
    assertEquals(closes, WaresWatch.closes(alive, buyer, vampire, fighting, distance));
  }
}
