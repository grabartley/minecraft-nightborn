package com.grahambartley.nightborn.trap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GroundingTest {

  @ParameterizedTest(name = "falling at {0} -> {1}")
  @CsvSource({"0.5, -0.8", "0, -0.8", "-0.2, -0.8", "-1.5, -1.5"})
  @DisplayName("a netted vampire is dragged down at least as fast as the net pulls")
  void falls(final double speed, final double dragged) {
    assertEquals(dragged, Grounding.fall(speed));
  }
}
