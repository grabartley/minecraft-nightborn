package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SetWearTest {

  @ParameterizedTest(name = "{0} armour pieces, blade held {1}: complete {2}")
  @CsvSource({
    "0, false, false",
    "0, true, false",
    "3, true, false",
    "4, false, false",
    "4, true, true",
    "5, true, true"
  })
  @DisplayName("a set is whole only with all four armour pieces worn and its blade in hand")
  void complete(final int pieces, final boolean blade, final boolean complete) {
    assertEquals(complete, SetWear.complete(pieces, blade));
  }
}
