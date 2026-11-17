package com.grahambartley.nightborn.gramophone;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GramophoneStateTest {

  @ParameterizedTest(name = "record {0}, playing {1}: {2}")
  @CsvSource({
    "false, false, EMPTY",
    "false, true, EMPTY",
    "true, false, LOADED",
    "true, true, PLAYING"
  })
  @DisplayName("a gramophone with no disc is empty whatever else is set")
  void of(final boolean hasRecord, final boolean playing, final GramophoneState state) {
    assertEquals(state, GramophoneState.of(hasRecord, playing));
  }
}
