package com.grahambartley.nightborn.inquisitor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class InquisitorFoesTest {

  @ParameterizedTest(name = "player {0}, vampire {1}, turned on {2}, peaceful {3}: seeks out {4}")
  @CsvSource({
    "true, true, false, false, true",
    "true, false, true, false, true",
    "true, false, false, false, false",
    "false, true, false, false, false",
    "true, true, false, true, false"
  })
  @DisplayName("he seeks out every vampire player and the human he turned on, never a bystander")
  void seeksOut(
      final boolean player,
      final boolean vampire,
      final boolean turned,
      final boolean peaceful,
      final boolean seeks) {
    assertEquals(seeks, InquisitorFoes.seeksOut(player, vampire, turned, peaceful));
  }

  @ParameterizedTest(name = "player {0}, vampire {1}, turned on {2}, struck him {3}: fights {4}")
  @CsvSource({
    "false, true, false, false, true",
    "true, true, false, false, true",
    "true, false, false, false, false",
    "true, false, true, false, true",
    "true, false, false, true, true",
    "false, false, false, true, false"
  })
  @DisplayName("he fights vampires, the human he turned on, and a human who strikes him")
  void fights(
      final boolean player,
      final boolean vampire,
      final boolean turned,
      final boolean struck,
      final boolean fights) {
    assertEquals(fights, InquisitorFoes.fights(player, vampire, turned, struck));
  }

  @ParameterizedTest(name = "untouchable {0}, from outside {1}, shrugged type {2}: shrugs off {3}")
  @CsvSource({
    "false, false, false, false",
    "true, false, false, true",
    "false, true, false, true",
    "false, false, true, true"
  })
  @DisplayName("he cannot be hurt while changing or warded, from outside, or by fire and falls")
  void shrugsOff(
      final boolean untouchable, final boolean outside, final boolean shrugged, final boolean off) {
    assertEquals(off, InquisitorFoes.shrugsOff(untouchable, outside, shrugged));
  }

  @ParameterizedTest(name = "ringer a vampire {0}, rang {1}: turns on {2}")
  @CsvSource({
    "false, true, true",
    "true, true, false",
    "false, false, false",
    "true, false, false"
  })
  @DisplayName("he turns on a human who rang the bell, never on a vampire or a hunt")
  void turnsOn(final boolean vampire, final boolean rang, final boolean turns) {
    assertEquals(turns, InquisitorFoes.turnsOn(vampire, rang));
  }
}
