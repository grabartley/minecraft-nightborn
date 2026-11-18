package com.grahambartley.nightborn.inquisitor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class InquisitorOutcomesTest {

  @ParameterizedTest(name = "bitten {0}, by a vampire {1}, by the turned human {2}: {3}")
  @CsvSource({
    "true, true, false, DRAINED",
    "false, false, true, STRUCK_DOWN",
    "true, false, false, OTHER",
    "false, true, false, OTHER",
    "false, false, false, OTHER"
  })
  @DisplayName("he ends drained by a vampire, struck down by the human he turned on, or otherwise")
  void death(
      final boolean bitten,
      final boolean vampire,
      final boolean turned,
      final InquisitorOutcomes.Death death) {
    assertEquals(death, InquisitorOutcomes.death(bitten, vampire, turned));
  }

  @ParameterizedTest(name = "{0}: pays {1}, clears notoriety {2}")
  @CsvSource({"DRAINED, true, true", "STRUCK_DOWN, true, false", "OTHER, false, false"})
  @DisplayName("both endings pay the vampires who struck him, but only draining him clears names")
  void rewards(final InquisitorOutcomes.Death death, final boolean pays, final boolean clears) {
    assertEquals(pays, InquisitorOutcomes.pays(death));
    assertEquals(clears, InquisitorOutcomes.clearsNotoriety(death));
  }

  @ParameterizedTest(name = "{0} after a bell {1}: comes back {2}")
  @CsvSource({"EMPTY, false, true", "EMPTY, true, false", "WON, false, false", "WON, true, false"})
  @DisplayName("on a hunt he comes back after an empty arena, never after a bell or a win")
  void comesBack(final InquisitorEnding ending, final boolean bell, final boolean back) {
    assertEquals(back, InquisitorOutcomes.comesBack(ending, bell));
  }
}
