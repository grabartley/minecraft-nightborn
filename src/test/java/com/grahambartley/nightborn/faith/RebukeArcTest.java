package com.grahambartley.nightborn.faith;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RebukeArcTest {

  @ParameterizedTest(name = "look ({0}, {1}), to ({2}, {3}), arc {4} -> {5}")
  @CsvSource({
    "0, 1, 0, 5, 90, true",
    "0, 1, 3, 4, 90, true",
    "0, 1, 5, 4, 90, false",
    "0, 1, 5, 0, 90, false",
    "0, 1, 0, -5, 90, false",
    "0, 1, 0, -5, 360, true",
    "0, 1, 0, 0, 90, true",
    "0, 0, 0, 5, 90, false"
  })
  @DisplayName("a rebuke reaches only those in the quarter circle in front")
  void inFront(
      final double lookX,
      final double lookZ,
      final double toX,
      final double toZ,
      final float arc,
      final boolean expected) {
    assertEquals(expected, RebukeArc.inFront(lookX, lookZ, toX, toZ, arc));
  }

  @ParameterizedTest(name = "holding {0}, vampire {1}, worn {2} -> {3}")
  @CsvSource({
    "true, false, 0, true",
    "false, false, 0.5, true",
    "false, false, 0, false",
    "true, true, 1, false"
  })
  @DisplayName("only a human holding or wearing a crucifix can rebuke")
  void armed(final boolean holding, final boolean vampire, final double worn, final boolean armed) {
    assertEquals(armed, RebukeArc.armed(holding, vampire, worn));
  }
}
