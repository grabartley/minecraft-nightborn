package com.grahambartley.nightborn.oldblood;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FontRefillTest {

  private static final long DAY = 24_000L;

  @ParameterizedTest(name = "{0} days with a household {1}: full after {2} days")
  @CsvSource({"7, true, 7", "7, false, 14", "1, true, 1", "1, false, 2"})
  @DisplayName("old blood refills over its days with a household and twice as long without")
  void refills(final int days, final boolean household, final int fullAfter) {
    final long nearly = FontRefill.advance(0L, fullAfter * DAY - 1, household, days);
    assertFalse(FontRefill.full(nearly));
    assertTrue(FontRefill.full(FontRefill.advance(nearly, 1L, household, days)));
  }

  @ParameterizedTest(name = "from {0}, {1} ticks: {2}")
  @CsvSource({"0, 0, 0", "0, -50, 0", "-1, 500, -1", "100, 50, 200"})
  @DisplayName("a full font stays full and time never runs backwards")
  void advances(final long from, final long elapsed, final long after) {
    assertEquals(after, FontRefill.advance(from, elapsed, true, 7));
  }

  @ParameterizedTest(name = "progress {0}: full {1}")
  @CsvSource({"-1, true", "0, false", "1000, false"})
  @DisplayName("only the full marker reads as full")
  void full(final long progress, final boolean full) {
    assertEquals(full, FontRefill.full(progress));
  }
}
