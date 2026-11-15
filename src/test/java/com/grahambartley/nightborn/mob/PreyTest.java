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

  @ParameterizedTest(name = "downed or escaping {0}, prey {1}, provoked {2}: bites {3}")
  @CsvSource({
    "false, true, false, true",
    "false, false, true, true",
    "false, false, false, false",
    "true, true, true, false",
    "true, true, false, false"
  })
  @DisplayName("a standing vampire mob bites prey, or anything that provoked it")
  void mayBite(
      final boolean downed, final boolean prey, final boolean provoked, final boolean bites) {
    assertEquals(bites, Prey.mayBite(downed, prey, provoked));
  }
}
