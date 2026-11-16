package com.grahambartley.nightborn.mind;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MindGuardTest {

  @ParameterizedTest(name = "mesmerised {0}, player {1}, by victim {2} -> blocked {3}")
  @CsvSource({
    "false, false, false, false",
    "false, true, true, false",
    "true, false, false, true",
    "true, false, true, true",
    "true, true, true, true",
    "true, true, false, false"
  })
  @DisplayName(
      "a mesmerised mob cannot hurt anyone and a mesmerised player cannot hurt the one who did it")
  void blocks(
      final boolean mesmerised,
      final boolean player,
      final boolean byVictim,
      final boolean expected) {
    assertEquals(expected, MindGuard.blocks(mesmerised, player, byVictim));
  }
}
