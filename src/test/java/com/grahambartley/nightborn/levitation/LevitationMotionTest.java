package com.grahambartley.nightborn.levitation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LevitationMotionTest {

  @ParameterizedTest(name = "levitating {0}, moves here {1}, server player {2}: {3}")
  @CsvSource({
    "true, true, false, true",
    "true, false, false, false",
    "false, true, false, false",
    "true, true, true, false",
    "false, false, true, false"
  })
  @DisplayName(
      "the hover runs where vanilla moves the body, never for a player on the server, whose own"
          + " client moves it")
  void drives(
      final boolean levitating,
      final boolean movesHere,
      final boolean serverPlayer,
      final boolean drives) {
    assertEquals(drives, LevitationMotion.drives(levitating, movesHere, serverPlayer));
  }
}
