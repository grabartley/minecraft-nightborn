package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class BossWardTest {

  @ParameterizedTest(name = "tagged as a keeper when loaded: {0}")
  @ValueSource(booleans = {true, false})
  @DisplayName("a keeper loaded from a save has no ward left to keep, so it goes")
  void orphaned(final boolean tagged) {
    assertEquals(tagged, BossWard.orphaned(tagged));
  }

  @ParameterizedTest(name = "was held {0}, held {1}, busy {2}, silent {3}: staggers {4}")
  @CsvSource({
    "true, false, false, false, true",
    "true, true, false, false, false",
    "false, false, false, false, false",
    "true, false, true, false, false",
    "true, false, false, true, false"
  })
  @DisplayName("the boss reels once when its ward falls, unless it is mid attack or has no clip")
  void broke(
      final boolean wasHeld,
      final boolean held,
      final boolean busy,
      final boolean silent,
      final boolean staggers) {
    assertEquals(staggers, BossWard.broke(wasHeld, held, busy, silent));
  }
}
