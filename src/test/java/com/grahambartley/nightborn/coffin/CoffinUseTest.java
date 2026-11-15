package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CoffinUseTest {

  @ParameterizedTest(name = "occupied {0}, sneaking {1}, vampire {2}: {3}")
  @CsvSource({
    "true, true, true, TOGGLE_LID",
    "true, true, false, TOGGLE_LID",
    "true, false, true, REFUSE_OCCUPIED",
    "true, false, false, REFUSE_OCCUPIED",
    "false, true, true, TOGGLE_LID",
    "false, true, false, TOGGLE_LID",
    "false, false, false, TOGGLE_LID",
    "false, false, true, REST"
  })
  @DisplayName(
      "a vampire lies down, anyone else or a sneaking player works the lid, and a full coffin opens only to a sneak")
  void decidesWhatUsingACoffinDoes(
      final boolean occupied, final boolean sneaking, final boolean vampire, final CoffinUse use) {
    assertEquals(use, CoffinUse.of(occupied, sneaking, vampire));
  }
}
