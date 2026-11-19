package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FrostWardTest {

  @ParameterizedTest(name = "ward {0}: {1}")
  @CsvSource({"0.0, false", "1.0, true", "0.5, true", "-1.0, false"})
  @DisplayName("any frost ward keeps the wearer from freezing and sinking")
  void warded(final double ward, final boolean warded) {
    assertEquals(warded, FrostWard.warded(ward));
  }
}
