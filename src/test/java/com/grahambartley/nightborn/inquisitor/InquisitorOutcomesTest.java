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

  @ParameterizedTest(name = "{0} after a bell {1}: {2}")
  @CsvSource({"EMPTY, false, COME_BACK", "EMPTY, true, END_VISIT", "WON, false, END_VISIT"})
  @DisplayName("his quarry is owed a return or an end to the visit when he leaves")
  void owedOnLeaving(
      final InquisitorEnding ending, final boolean bell, final InquisitorLedger.Owed owed) {
    assertEquals(java.util.List.of(owed), InquisitorOutcomes.owedOnLeaving(ending, bell));
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {"DRAINED | CLEAR_NOTORIETY END_VISIT PAY ENDED", "STRUCK_DOWN | PAY", "OTHER | ''"})
  @DisplayName("each vampire who struck him is owed what his ending pays")
  void owedToFighter(final InquisitorOutcomes.Death death, final String owed) {
    assertEquals(
        owed.isBlank()
            ? java.util.List.of()
            : java.util.Arrays.stream(owed.split(" ")).map(InquisitorLedger.Owed::valueOf).toList(),
        InquisitorOutcomes.owedToFighter(death));
  }
}
