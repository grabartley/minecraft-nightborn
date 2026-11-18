package com.grahambartley.nightborn.wheel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WheelGateTest {

  @ParameterizedTest(name = "main hand {0}, own use {1}, spectator {2} -> {3}")
  @CsvSource({
    "true, false, false, true",
    "false, false, false, false",
    "true, true, false, false",
    "true, false, true, false",
    "false, true, true, false"
  })
  @DisplayName("the wheel opens from the main hand unless the held item has a use of its own")
  void opens(
      final boolean mainHand,
      final boolean keepsOwnUse,
      final boolean spectator,
      final boolean opens) {
    assertEquals(opens, WheelGate.opens(mainHand, keepsOwnUse, spectator));
  }
}
