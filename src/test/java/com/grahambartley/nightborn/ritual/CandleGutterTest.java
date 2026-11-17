package com.grahambartley.nightborn.ritual;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CandleGutterTest {

  @ParameterizedTest(name = "tended={0} flame={1} lit={2} -> {3}")
  @CsvSource({
    "true, true, true, true",
    "true, true, false, false",
    "true, false, false, false",
    "false, true, true, false"
  })
  @DisplayName("only a lit light that vampires tend can gutter")
  void burning(
      final boolean tended, final boolean hasFlame, final boolean lit, final boolean burns) {
    assertEquals(burns, CandleGutter.burning(tended, hasFlame, lit));
  }
}
