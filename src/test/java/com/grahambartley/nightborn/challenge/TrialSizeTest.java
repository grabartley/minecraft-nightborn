package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TrialSizeTest {

  @ParameterizedTest(name = "a {0} trial pays {1} of the next point")
  @CsvSource({"SMALL, 0.25", "MEDIUM, 0.5", "LARGE, 1.0"})
  void paysItsShareOfTheNextPoint(final TrialSize size, final float share) {
    assertEquals(share, size.share());
  }
}
