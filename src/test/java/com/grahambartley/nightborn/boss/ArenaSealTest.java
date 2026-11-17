package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ArenaSealTest {

  @ParameterizedTest(name = "was inside {0}, inside {1}, a foe {2}: turned back {3}")
  @CsvSource({
    "true, false, true, true",
    "true, true, true, false",
    "false, false, true, false",
    "false, true, true, false",
    "true, false, false, false"
  })
  @DisplayName(
      "the wall lets anyone in and turns back whoever was inside, unless the boss will not fight"
          + " them")
  void turnsBack(
      final boolean wasInside, final boolean inside, final boolean foe, final boolean back) {
    assertEquals(back, ArenaSeal.turnsBack(wasInside, inside, foe));
  }
}
