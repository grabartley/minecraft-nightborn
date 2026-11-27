package com.grahambartley.nightborn.guild.rite;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RiteBreaksTest {

  @ParameterizedTest(
      name = "hunter here {0} hurt {1} moved² {2}, master here {3} hurt {4} fighting {5} -> {6}")
  @CsvSource({
    "true, false, 0.0, true, false, false, false",
    "true, false, 3.9, true, false, false, false",
    "true, false, 4.0, true, false, false, false",
    "true, false, 4.1, true, false, false, true",
    "true, true, 0.0, true, false, false, true",
    "false, false, 0.0, true, false, false, true",
    "true, false, 0.0, false, false, false, true",
    "true, false, 0.0, true, true, false, true",
    "true, false, 0.0, true, false, true, true"
  })
  @DisplayName("walking off, a wound, or a Guild Master hurt, fighting or gone breaks the rite")
  void broken(
      final boolean here,
      final boolean hurt,
      final double movedSq,
      final boolean masterHere,
      final boolean masterHurt,
      final boolean fighting,
      final boolean broken) {
    assertEquals(
        broken,
        RiteBreaks.broken(
            new RiteBreaks.Hunter(here, hurt, movedSq),
            new RiteBreaks.Master(masterHere, masterHurt, fighting),
            2.0));
  }
}
