package com.grahambartley.nightborn.levitation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LevitationMotionTest {

  @ParameterizedTest(name = "levitating {0}, client {1}, moves here {2}, player {3}: {4}")
  @CsvSource({
    "true, false, true, false, true",
    "true, false, false, false, true",
    "true, false, true, true, false",
    "true, true, true, true, true",
    "true, true, false, true, false",
    "true, true, false, false, false",
    "true, true, true, false, true",
    "false, false, true, false, false",
    "false, true, true, true, false"
  })
  @DisplayName(
      "the server holds up every levitating mob, even one with no AI, and a player's own client"
          + " holds up the player")
  void drives(
      final boolean levitating,
      final boolean client,
      final boolean movesHere,
      final boolean player,
      final boolean drives) {
    assertEquals(drives, LevitationMotion.drives(levitating, client, movesHere, player));
  }
}
