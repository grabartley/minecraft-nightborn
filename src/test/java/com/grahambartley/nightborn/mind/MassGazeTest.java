package com.grahambartley.nightborn.mind;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MassGazeTest {

  @ParameterizedTest(name = "mob {0}, player {1}, vampire {2}, seen {3}, allied {4}: gazes {5}")
  @CsvSource({
    "true, false, false, true, false, true",
    "true, false, true, true, false, true",
    "false, true, false, true, false, true",
    "false, true, true, true, false, false",
    "true, false, false, false, false, false",
    "false, false, false, true, false, false",
    "true, false, true, true, true, false",
    "false, true, false, true, true, false"
  })
  @DisplayName(
      "the gaze takes every creature and human player in plain sight, but never the caster's allies")
  void gazes(
      final boolean mob,
      final boolean player,
      final boolean vampire,
      final boolean seen,
      final boolean allied,
      final boolean gazes) {
    assertEquals(gazes, MassGaze.gazes(mob, player, vampire, seen, allied));
  }
}
