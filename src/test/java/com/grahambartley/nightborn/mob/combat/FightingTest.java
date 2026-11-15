package com.grahambartley.nightborn.mob.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FightingTest {

  @ParameterizedTest(name = "target {0}, downed {1}, asleep {2}, retreating {3}, weak {4}: {5}")
  @CsvSource({
    "true, false, false, false, false, true",
    "false, false, false, false, false, false",
    "true, true, false, false, false, false",
    "true, false, true, false, false, false",
    "true, false, false, true, false, false",
    "true, false, false, false, true, false"
  })
  @DisplayName(
      "a vampire casts and moves only with a target, and never while downed, asleep,"
          + " falling back or weakened")
  void fights(
      final boolean target,
      final boolean downed,
      final boolean asleep,
      final boolean retreating,
      final boolean weakened,
      final boolean fights) {
    assertEquals(fights, Fighting.fights(target, downed, asleep, retreating, weakened));
  }
}
