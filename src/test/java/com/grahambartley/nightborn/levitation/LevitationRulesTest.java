package com.grahambartley.nightborn.levitation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LevitationRulesTest {

  @ParameterizedTest(name = "vampire {0}, weakened {1}, downed {2}: {3}")
  @CsvSource({
    "true, 0, false, true",
    "false, 0, false, false",
    "true, 1, false, false",
    "true, 100, false, false",
    "true, 0, true, false"
  })
  @DisplayName("only a vampire that is neither weakened nor downed can levitate")
  void mayLevitate(
      final boolean vampire, final int weakened, final boolean downed, final boolean may) {
    assertEquals(may, LevitationRules.mayLevitate(vampire, weakened, downed));
  }
}
