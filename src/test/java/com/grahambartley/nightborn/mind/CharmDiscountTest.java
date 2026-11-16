package com.grahambartley.nightborn.mind;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CharmDiscountTest {

  @ParameterizedTest(name = "price {0}, charm {1} -> cut {2}")
  @CsvSource({
    "1, 1, 1",
    "3, 1, 1",
    "10, 1, 3",
    "20, 1, 6",
    "20, 2, 7",
    "64, 5, 35",
    "10, 0, 0",
    "0, 1, 0"
  })
  @DisplayName("a charmed price drops as a hero of the village's would at the same level")
  void cut(final int price, final int charm, final int expected) {
    assertEquals(expected, CharmDiscount.cut(price, charm));
  }

  @ParameterizedTest(name = "charmed {0}, charm {1}, hero {2} -> {3}")
  @CsvSource({
    "true, 1, false, true",
    "false, 1, false, false",
    "true, 0, false, false",
    "true, 1, true, false"
  })
  @DisplayName("only the charmer gets the discount, and never on top of a hero's")
  void applies(final boolean charmed, final int charm, final boolean hero, final boolean expected) {
    assertEquals(expected, CharmDiscount.applies(charmed, charm, hero));
  }
}
