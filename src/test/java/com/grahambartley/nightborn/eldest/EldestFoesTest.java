package com.grahambartley.nightborn.eldest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EldestFoesTest {

  @ParameterizedTest(name = "vampire={0} player={1} struck={2} -> {3}")
  @CsvSource({
    "false, true, false, true",
    "false, false, false, true",
    "true, true, false, false",
    "true, true, true, true",
    "true, false, true, false",
    "true, false, false, false"
  })
  @DisplayName("the Eldest fights humans, and a vampire player only once struck by one")
  void fights(
      final boolean vampire, final boolean player, final boolean struck, final boolean fights) {
    assertEquals(fights, EldestFoes.fights(vampire, player, struck));
  }

  @ParameterizedTest(name = "vampire={0} peaceful={1} -> {2}")
  @CsvSource({
    "false, false, true",
    "false, true, false",
    "true, false, false",
    "true, true, false"
  })
  @DisplayName("the Eldest seeks out humans, but not on Peaceful")
  void seeksOut(final boolean vampire, final boolean peaceful, final boolean seeks) {
    assertEquals(seeks, EldestFoes.seeksOut(vampire, peaceful));
  }

  @ParameterizedTest(
      name = "changing phase {0}, struck from outside {1}, a finishing blow {2}: {3}")
  @CsvSource({
    "false, false, false, false",
    "true, false, false, true",
    "false, true, false, true",
    "true, true, false, true",
    "true, false, true, false",
    "false, true, true, false"
  })
  @DisplayName(
      "the Eldest shrugs off blows while it changes phase and from outside its arena, but never a"
          + " stake or the sun")
  void shrugsOff(
      final boolean changing,
      final boolean fromOutside,
      final boolean finishing,
      final boolean shrugged) {
    assertEquals(shrugged, EldestFoes.shrugsOff(changing, fromOutside, finishing));
  }
}
