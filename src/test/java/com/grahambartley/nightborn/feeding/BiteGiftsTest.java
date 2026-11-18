package com.grahambartley.nightborn.feeding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BiteGiftsTest {

  @ParameterizedTest(name = "{0} x {1} = {2}")
  @CsvSource({"10.0, 1.0, 10.0", "10.0, 1.1, 11.0", "0.0, 1.1, 0.0", "10.0, 0.5, 10.0"})
  @DisplayName("a feeding bite's blood grows with the gift and never shrinks")
  void feeding(final float worth, final double multiplier, final float given) {
    assertEquals(given, BiteGifts.feeding(worth, multiplier), 1.0E-4F);
  }

  @ParameterizedTest(name = "{0} + {1} = {2}")
  @CsvSource({"6.0, 2.0, 8.0", "6.0, 0.0, 6.0", "0.0, 2.0, 0.0", "6.0, -1.0, 6.0"})
  @DisplayName("a finisher gives its extra blood only when it drew some")
  void finisher(final float worth, final double extra, final float given) {
    assertEquals(given, BiteGifts.finisher(worth, extra), 1.0E-4F);
  }
}
