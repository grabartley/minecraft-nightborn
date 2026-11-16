package com.grahambartley.nightborn.servant;

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

  @ParameterizedTest(name = "owner age {0}, last struck at {1} -> {2}")
  @CsvSource({
    "1000, 1000, true",
    "1000, 950, true",
    "1000, 900, true",
    "1000, 899, false",
    "1000, 0, false",
    "10, 20, false"
  })
  @DisplayName("a thrall joins its owner's fight only while the owner is still fighting")
  void ownerStillFighting(final int age, final int lastAttack, final boolean expected) {
    assertEquals(expected, TargetRules.ownerStillFighting(age, lastAttack));
  }
}
