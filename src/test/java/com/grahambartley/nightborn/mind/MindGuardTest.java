package com.grahambartley.nightborn.mind;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MindGuardTest {

  @ParameterizedTest(
      name = "mesmerised {0}, player {1}, by victim {2}, explosion {3} -> blocked {4}")
  @CsvSource({
    "false, false, false, false, false",
    "false, true, true, false, false",
    "true, false, false, false, true",
    "true, false, true, false, true",
    "true, true, true, false, true",
    "true, true, false, false, false",
    "true, false, false, true, false"
  })
  @DisplayName(
      "a mesmerised mob cannot strike anyone, though a lit fuse still blows, and a mesmerised player cannot hurt the one who did it")
  void blocks(
      final boolean mesmerised,
      final boolean player,
      final boolean byVictim,
      final boolean explosion,
      final boolean expected) {
    assertEquals(expected, MindGuard.blocks(mesmerised, player, byVictim, explosion));
  }
}
