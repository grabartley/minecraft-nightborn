package com.grahambartley.nightborn.servant.job;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ScoutSightingTest {

  @ParameterizedTest(name = "hunter {0}, vampire {1}, villager {2}, player {3} -> {4}")
  @CsvSource({
    "true, false, false, false, HUNTER",
    "false, true, false, false, VAMPIRE",
    "false, true, false, true, VAMPIRE",
    "false, false, true, false, VILLAGER",
    "false, false, false, true, HUMAN",
    "false, false, false, false, NONE",
    "true, true, true, true, HUNTER"
  })
  @DisplayName("a scout counts each soul once, as a hunter, a vampire, a villager or a traveller")
  void of(
      final boolean hunter,
      final boolean vampire,
      final boolean villager,
      final boolean player,
      final ScoutSighting sighting) {
    assertEquals(sighting, ScoutSighting.of(hunter, vampire, villager, player));
  }
}
