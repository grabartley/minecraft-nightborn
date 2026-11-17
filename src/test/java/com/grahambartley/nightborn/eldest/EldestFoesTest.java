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
      name = "changing phase {0}, from outside {1}, from its surroundings {2}, finishing {3}: {4}")
  @CsvSource({
    "false, false, false, false, false",
    "true, false, false, false, true",
    "false, true, false, false, true",
    "false, false, true, false, true",
    "true, true, true, false, true",
    "true, false, false, true, false",
    "false, true, false, true, false",
    "false, false, true, true, false"
  })
  @DisplayName(
      "the Eldest shrugs off blows while it changes phase, from outside its arena and from fire,"
          + " falls and the like, but never what finishes a vampire")
  void shrugsOff(
      final boolean changing,
      final boolean fromOutside,
      final boolean surroundings,
      final boolean finishing,
      final boolean shrugged) {
    assertEquals(shrugged, EldestFoes.shrugsOff(changing, fromOutside, surroundings, finishing));
  }

  @ParameterizedTest(name = "a seat {0}: mounts {1}")
  @CsvSource({"true, true", "false, false"})
  @DisplayName("the Eldest sits in a chair but never rides a boat or a cart")
  void mounts(final boolean seat, final boolean mounts) {
    assertEquals(mounts, EldestFoes.mounts(seat));
  }
}
