package com.grahambartley.nightborn.batform;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BatFlightMotionTest {

  @ParameterizedTest(name = "bat {0}, client player {1}, moves here {2}, in fluid {3}: {4}")
  @CsvSource({
    "true, true, true, false, true",
    "false, true, true, false, false",
    "true, false, true, false, false",
    "true, true, false, false, false",
    "true, true, true, true, false"
  })
  @DisplayName(
      "bat flight runs for a bat player on the client that moves it, and gives way to swimming")
  void drives(
      final boolean bat,
      final boolean clientPlayer,
      final boolean movesHere,
      final boolean inFluid,
      final boolean drives) {
    assertEquals(drives, BatFlightMotion.drives(bat, clientPlayer, movesHere, inFluid));
  }
}
