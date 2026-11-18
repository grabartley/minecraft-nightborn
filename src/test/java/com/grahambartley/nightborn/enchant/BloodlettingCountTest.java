package com.grahambartley.nightborn.enchant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BloodlettingCountTest {

  @ParameterizedTest(name = "{0} blows so far, 1 blood per {1}: now {2}, bleeds {3}")
  @CsvSource({
    "0, 4, 1, false",
    "2, 4, 3, false",
    "3, 4, 0, true",
    "1, 2, 0, true",
    "0, 2, 1, false",
    "2, 3, 0, true",
    "-5, 4, 1, false",
    "0, 0, 1, false"
  })
  @DisplayName("Bloodletting gives one blood point every so many blows")
  void counts(final int soFar, final int per, final int blows, final boolean bleeds) {
    assertEquals(new BloodlettingCount(blows, bleeds), BloodlettingCount.after(soFar, per));
  }
}
