package com.grahambartley.nightborn.levitation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LevitationRulesTest {

  @ParameterizedTest(name = "vampire {0}, weakened {1}, downed {2}, occupied {3}: {4}")
  @CsvSource({
    "true, 0, false, false, true",
    "false, 0, false, false, false",
    "true, 1, false, false, false",
    "true, 100, false, false, false",
    "true, 0, true, false, false",
    "true, 0, false, true, false"
  })
  @DisplayName("only a free vampire that is neither weakened nor downed can levitate")
  void mayLevitate(
      final boolean vampire,
      final int weakened,
      final boolean downed,
      final boolean occupied,
      final boolean may) {
    assertEquals(may, LevitationRules.mayLevitate(vampire, weakened, downed, occupied));
  }

  @ParameterizedTest(name = "last {0}, now {1}: {2}")
  @CsvSource({
    ", 100, true",
    "100, 100, false",
    "100, 109, false",
    "100, 110, true",
    "100, 50, true"
  })
  @DisplayName("the toggle waits half a second between presses")
  void toggleReady(final Long last, final long now, final boolean ready) {
    assertEquals(ready, LevitationRules.toggleReady(last, now));
  }

  @ParameterizedTest(name = "on ground {0}, levitating {1}: {2}")
  @CsvSource({"true, false, true", "false, true, true", "true, true, true", "false, false, false"})
  @DisplayName(
      "a levitating vampire steps up and starts a sprint as it would standing on the ground")
  void movesAsGrounded(final boolean onGround, final boolean levitating, final boolean grounded) {
    assertEquals(grounded, LevitationRules.movesAsGrounded(onGround, levitating));
  }
}
