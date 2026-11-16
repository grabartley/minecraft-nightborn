package com.grahambartley.nightborn.thrall;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ThrallModeTest {

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"FOLLOW, STAY", "STAY, GUARD", "GUARD, FOLLOW"})
  @DisplayName("each order moves the thrall on to the next of follow, stay and guard")
  void next(final ThrallMode mode, final ThrallMode expected) {
    assertEquals(expected, mode.next());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "FOLLOW, nightborn.thrall.mode.follow",
    "STAY, nightborn.thrall.mode.stay",
    "GUARD, nightborn.thrall.mode.guard"
  })
  @DisplayName("each order names itself to the owner")
  void keys(final ThrallMode mode, final String key) {
    assertEquals(key, mode.translationKey());
  }

  @ParameterizedTest(name = "\"{0}\" -> {1}")
  @CsvSource({"FOLLOW, FOLLOW", "STAY, STAY", "GUARD, GUARD", "'', FOLLOW", "nonsense, FOLLOW"})
  @DisplayName("a saved order loads back, and anything unknown follows")
  void byName(final String name, final ThrallMode expected) {
    assertEquals(expected, ThrallMode.byName(name));
  }
}
