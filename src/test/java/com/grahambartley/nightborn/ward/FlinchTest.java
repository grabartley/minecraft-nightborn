package com.grahambartley.nightborn.ward;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FlinchTest {

  @ParameterizedTest(name = "was in reach {0}, in reach {1} -> {2}")
  @CsvSource({
    "false, true, true",
    "true, true, false",
    "true, false, false",
    "false, false, false"
  })
  @DisplayName("a vampire flinches once, on first coming into reach")
  void starts(final boolean was, final boolean now, final boolean flinches) {
    assertEquals(flinches, Flinch.starts(was, now));
  }

  @ParameterizedTest(name = "strings {0}, crucifix {1}, shape {2} -> {3}")
  @CsvSource({
    "0, 0, 0, false",
    "1, 0, 0, true",
    "0, 30, 0, true",
    "0, 0, 12, true",
    "5, 5, 5, true"
  })
  @DisplayName("garlic strings, a held crucifix and a crucifix shape all reach a vampire")
  void inReach(final int strings, final int crucifix, final int shape, final boolean reached) {
    assertEquals(reached, Flinch.inReach(strings, crucifix, shape));
  }
}
