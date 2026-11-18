package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PowerReachTest {

  @ParameterizedTest(
      name =
          "alive {0}, self {1}, serves {2}, untouchable {3}, caster player {4},"
              + " target player {5}, pvp {6}: touches {7}")
  @CsvSource({
    "true, false, false, false, true, false, false, true",
    "true, false, false, false, true, true, false, false",
    "true, false, false, false, true, true, true, true",
    "true, false, false, false, false, true, false, true",
    "false, false, false, false, true, false, true, false",
    "true, true, false, false, true, false, true, false",
    "true, false, true, false, true, false, true, false",
    "true, false, false, true, true, false, true, false"
  })
  @DisplayName(
      "a power touches another player only with PvP on, and never the caster, its servants,"
          + " the dead or the untouchable")
  void touches(
      final boolean alive,
      final boolean self,
      final boolean serves,
      final boolean untouchable,
      final boolean casterPlayer,
      final boolean targetPlayer,
      final boolean pvp,
      final boolean touches) {
    assertEquals(
        touches,
        PowerReach.touches(alive, self, serves, untouchable, casterPlayer, targetPlayer, pvp));
  }
}
