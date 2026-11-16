package com.grahambartley.nightborn.mob.household;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RescanTest {

  @ParameterizedTest(name = "scanned at {0}, now {1}: {2}")
  @CsvSource({
    "-9223372036854775808, 0, true",
    "100, 100, false",
    "100, 1299, false",
    "100, 1300, true",
    "5000, 100, true"
  })
  @DisplayName("a home's furniture is looked up again after a while, or after time jumps back")
  void due(final long scannedAt, final long now, final boolean due) {
    assertEquals(due, Rescan.due(scannedAt, now, 1200L));
  }
}
