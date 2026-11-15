package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SeatingTest {

  @ParameterizedTest(name = "sneaking={0} riding={1} taken={2} -> {3}")
  @CsvSource({
    "false, false, false, SIT",
    "false, false, true, TAKEN",
    "true, false, false, PASS",
    "false, true, false, PASS",
    "true, true, true, PASS"
  })
  void seatsOnlyAStandingPlayerOnAFreeChair(
      final boolean sneaking,
      final boolean riding,
      final boolean taken,
      final Seating.Choice expected) {
    assertEquals(expected, Seating.choose(sneaking, riding, taken));
  }

  @ParameterizedTest(name = "sitter={0} chair={1} keeps={2}")
  @CsvSource({
    "true, true, true",
    "false, true, false",
    "true, false, false",
    "false, false, false"
  })
  void keepsTheSeatOnlyWhileSomeoneSitsOnAChair(
      final boolean sitter, final boolean chair, final boolean keeps) {
    assertEquals(keeps, Seating.keepsSeat(sitter, chair));
  }
}
