package com.grahambartley.nightborn.mob.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GrudgeTest {

  @ParameterizedTest(name = "attacker present {0}, downed {1}, {2} blocks, follow {3}: holds {4}")
  @CsvSource({
    "true, false, 3, 32, true",
    "true, false, 32, 32, true",
    "true, false, 32.5, 32, false",
    "false, false, 3, 32, false",
    "true, true, 3, 32, false",
    "false, true, 40, 32, false"
  })
  @DisplayName(
      "a vampire keeps its grudge until its attacker dies, turns creative or spectator, gets out"
          + " of reach, or the vampire is downed")
  void holds(
      final boolean present,
      final boolean downed,
      final double distance,
      final double followRange,
      final boolean holds) {
    assertEquals(holds, Grudge.holds(present, downed, distance, followRange));
  }

  @Test
  @DisplayName("a fresh grudge is held against nobody")
  void freshGrudgeIsEmpty() {
    final Grudge grudge = new Grudge();
    assertFalse(grudge.held());
    assertFalse(grudge.against(null));
  }

  @Test
  @DisplayName("forgiving a target the vampire holds no grudge against changes nothing")
  void forgivingNobodyKeepsItEmpty() {
    final Grudge grudge = new Grudge();
    grudge.forgive(null);
    assertFalse(grudge.held());
  }
}
