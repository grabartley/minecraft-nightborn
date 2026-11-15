package com.grahambartley.nightborn.familiar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FamiliarHiringTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "HIRE, nightborn.familiar.hire.hire",
    "NOT_A_VAMPIRE, nightborn.familiar.hire.not_a_vampire",
    "NOT_HIREABLE, nightborn.familiar.hire.not_hireable",
    "ALREADY_KEEPS_ONE, nightborn.familiar.hire.already_keeps_one"
  })
  @DisplayName("each hiring outcome tells the player what happened")
  void key(final HireRules.Outcome outcome, final String key) {
    assertEquals(key, FamiliarHiring.key(outcome));
  }
}
