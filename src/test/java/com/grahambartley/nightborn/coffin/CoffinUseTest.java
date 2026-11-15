package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CoffinUseTest {

  @ParameterizedTest(name = "occupied {0}, sneaking {1}, empty-handed {2}, vampire {3}: {4}")
  @CsvSource({
    "true, true, true, true, TOGGLE_LID",
    "true, true, true, false, TOGGLE_LID",
    "true, true, false, true, REFUSE_OCCUPIED",
    "true, true, false, false, REFUSE_OCCUPIED",
    "true, false, true, true, REFUSE_OCCUPIED",
    "true, false, true, false, REFUSE_OCCUPIED",
    "true, false, false, true, REFUSE_OCCUPIED",
    "true, false, false, false, REFUSE_OCCUPIED",
    "false, true, true, true, TOGGLE_LID",
    "false, true, false, true, TOGGLE_LID",
    "false, true, true, false, TOGGLE_LID",
    "false, false, true, false, TOGGLE_LID",
    "false, false, false, false, TOGGLE_LID",
    "false, false, true, true, REST",
    "false, false, false, true, REST"
  })
  @DisplayName(
      "a vampire lies down, anyone else or a sneaking vampire works the lid, and a full coffin opens only to an empty-handed sneak")
  void decidesWhatUsingACoffinDoes(
      final boolean occupied,
      final boolean sneaking,
      final boolean emptyHanded,
      final boolean vampire,
      final CoffinUse use) {
    assertEquals(use, CoffinUse.of(occupied, sneaking, emptyHanded, vampire));
  }
}
