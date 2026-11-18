package com.grahambartley.nightborn.raid;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HouseGuardsTest {

  @ParameterizedTest(name = "player {0}, vampire {1}, creative {2}, hunter mob {3}: {4}")
  @CsvSource({
    "true, false, false, false, true",
    "true, true, false, false, false",
    "true, false, true, false, false",
    "false, false, false, true, true",
    "false, false, false, false, false",
    "false, true, false, false, false"
  })
  @DisplayName("guards take human players and hunters for intruders, and let vampires by")
  void intrudes(
      final boolean player,
      final boolean vampire,
      final boolean creative,
      final boolean hunterMob,
      final boolean intrudes) {
    assertEquals(intrudes, HouseGuards.intrudes(player, vampire, creative, hunterMob));
  }

  @ParameterizedTest(name = "monster {0}, vampire {1}, keeps a house {2}: guards against {3}")
  @CsvSource({
    "true, false, false, true",
    "true, true, false, true",
    "true, false, true, true",
    "true, true, true, false",
    "false, false, true, false",
    "false, false, false, false"
  })
  @DisplayName(
      "a player's thrall fights any monster, but a house's thrall never turns on the house's"
          + " vampires")
  void guardsAgainst(
      final boolean monster,
      final boolean vampire,
      final boolean keepsHouse,
      final boolean guards) {
    assertEquals(guards, HouseGuards.guardsAgainst(monster, vampire, keepsHouse));
  }
}
