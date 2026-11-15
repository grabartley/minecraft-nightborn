package com.grahambartley.nightborn.levitation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LevitationMotionTest {

  @ParameterizedTest(name = "levitating {0}, moves here {1}: {2}")
  @CsvSource({
    "true, true, true",
    "true, false, false",
    "false, true, false",
    "false, false, false"
  })
  @DisplayName(
      "the hover runs only where vanilla moves the body: the server for mobs, the client for its player")
  void drives(final boolean levitating, final boolean movesHere, final boolean drives) {
    assertEquals(drives, LevitationMotion.drives(levitating, movesHere));
  }
}
