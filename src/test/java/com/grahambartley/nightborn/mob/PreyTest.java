package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PreyTest {

  @ParameterizedTest(name = "warm blood {0}, vampire {1}, untouchable player {2}: prey {3}")
  @CsvSource({
    "true, false, false, true",
    "false, false, false, false",
    "true, true, false, false",
    "true, false, true, false"
  })
  @DisplayName("only warm-blooded creatures that are not vampires are prey")
  void prey(
      final boolean warmBlood,
      final boolean vampire,
      final boolean untouchable,
      final boolean prey) {
    assertEquals(prey, Prey.isPrey(warmBlood, vampire, untouchable));
  }

  @ParameterizedTest(
      name = "downed or escaping {0}, vampire victim {1}, prey {2}, provoked {3}: bites {4}")
  @CsvSource({
    "false, false, true, false, true",
    "false, false, false, true, true",
    "false, false, false, false, false",
    "true, false, true, true, false",
    "true, false, true, false, false",
    "false, true, false, true, false",
    "false, true, true, true, false"
  })
  @DisplayName(
      "a standing vampire mob bites prey, or anything that provoked it, but never another vampire")
  void mayBite(
      final boolean downed,
      final boolean vampire,
      final boolean prey,
      final boolean provoked,
      final boolean bites) {
    assertEquals(bites, Prey.mayBite(downed, vampire, prey, provoked));
  }

  @ParameterizedTest(name = "peaceful {0}, player {1}: spared {2}")
  @CsvSource({
    "true, true, true",
    "true, false, false",
    "false, true, false",
    "false, false, false"
  })
  @DisplayName("on peaceful a vampire mob never hunts players, but still hunts animals")
  void spared(final boolean peaceful, final boolean player, final boolean spared) {
    assertEquals(spared, Prey.spared(peaceful, player));
  }
}
