package com.grahambartley.nightborn.inquisitor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class InquisitorEndingTest {

  @ParameterizedTest(name = "{0} inside, {1} standing, empty {2} of {3} ticks: {4}")
  @CsvSource({
    "1, 1, 0, 600, NONE",
    "2, 1, 9999, 600, NONE",
    "0, 0, 0, 600, WON",
    "0, 1, 599, 600, NONE",
    "0, 1, 600, 600, EMPTY",
    "0, 2, 5000, 600, EMPTY"
  })
  @DisplayName("he wins when every foe he met has fallen, and leaves an arena empty too long")
  void ends(
      final int inside,
      final int standing,
      final long empty,
      final long limit,
      final InquisitorEnding ending) {
    assertEquals(ending, InquisitorEnding.of(inside, standing, empty, limit));
  }
}
