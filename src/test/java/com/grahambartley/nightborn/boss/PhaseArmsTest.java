package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PhaseArmsTest {

  @ParameterizedTest(name = "hand empty {0}, holds it {1}, phase wants one {2}: swaps {3}")
  @CsvSource({
    "true, false, true, true",
    "false, false, true, true",
    "false, true, true, false",
    "true, false, false, false",
    "false, false, false, true",
    "false, true, false, true"
  })
  @DisplayName("the boss draws its phase's weapon once and puts it away when the phase has none")
  void changes(
      final boolean handEmpty, final boolean holdsIt, final boolean wantsOne, final boolean swaps) {
    assertEquals(swaps, PhaseArms.changes(handEmpty, holdsIt, wantsOne));
  }
}
