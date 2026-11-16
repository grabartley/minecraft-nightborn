package com.grahambartley.nightborn.thrall;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TargetRulesTest {

  @ParameterizedTest(name = "alive {0}, owner {1}, fellow {2}, untouchable {3} -> {4}")
  @CsvSource({
    "true, false, false, false, true",
    "false, false, false, false, false",
    "true, true, false, false, false",
    "true, false, true, false, false",
    "true, false, false, true, false"
  })
  @DisplayName("a thrall never turns on its owner, its owner's other thralls or a creative player")
  void mayAttack(
      final boolean alive,
      final boolean owner,
      final boolean fellow,
      final boolean untouchable,
      final boolean expected) {
    assertEquals(expected, TargetRules.mayAttack(alive, owner, fellow, untouchable));
  }
}
