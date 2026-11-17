package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BossPowersTest {

  @ParameterizedTest(name = "{0} blood and a cost of {1}: {2}")
  @CsvSource({"0, 4, 4", "3, 4, 4", "4, 4, 4", "9, 4, 9", "5, 0, 5"})
  @DisplayName("a boss always has the blood its power costs and never loses what it had")
  void fed(final int blood, final int cost, final int fed) {
    assertEquals(fed, BossPowers.fed(blood, cost));
  }
}
