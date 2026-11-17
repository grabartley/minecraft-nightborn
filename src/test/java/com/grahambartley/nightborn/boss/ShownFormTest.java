package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ShownFormTest {

  @ParameterizedTest(name = "idle {0}, walk {1}, form {2}, blazing {3}")
  @CsvSource(
      value = {
        "beast_idle, beast_walk, form_beast, false",
        "fly, fly, form_fly, false",
        "blood_idle, blood_walk, form_blood, true",
        "'', '', form_downed, false",
        "'', '', '', false"
      })
  @DisplayName("a shown form survives being packed into one synced string")
  void roundTrips(final String idle, final String walk, final String form, final boolean blazing) {
    final ShownForm shown = new ShownForm(idle, walk, form, blazing);
    assertEquals(shown, ShownForm.decode(shown.encode()));
  }

  @ParameterizedTest(name = "\"{0}\" reads as nothing shown")
  @CsvSource(value = {"''", "idle", "a|b", "a|b|c|1|extra"})
  @DisplayName("a string that is not a packed form shows nothing")
  void rejectsOtherStrings(final String packed) {
    assertEquals(ShownForm.NONE, ShownForm.decode(packed));
  }

  @ParameterizedTest(name = "still {0}, walking {1}, idle \"{2}\", walk \"{3}\": plays \"{4}\"")
  @CsvSource(
      value = {
        "true, false, beast_idle, beast_walk, beast_idle",
        "false, true, beast_idle, beast_walk, beast_walk",
        "false, false, beast_idle, beast_walk, ''",
        "true, false, '', beast_walk, ''",
        "false, true, beast_idle, '', ''"
      })
  @DisplayName("the phase's own idle and walk replace the shared ones, and nothing else")
  void picksTheClip(
      final boolean still,
      final boolean walking,
      final String idle,
      final String walk,
      final String plays) {
    assertEquals(
        plays.isEmpty() ? Optional.empty() : Optional.of(plays),
        new ShownForm(idle, walk, "", false).clip(still, walking));
  }

  @ParameterizedTest(name = "blazing {0}")
  @CsvSource({"true", "false"})
  @DisplayName("the rage glow is set without touching the clips")
  void setsTheGlow(final boolean blazing) {
    assertEquals(
        new ShownForm("a", "b", "c", blazing),
        new ShownForm("a", "b", "c", !blazing).blazing(blazing));
  }
}
