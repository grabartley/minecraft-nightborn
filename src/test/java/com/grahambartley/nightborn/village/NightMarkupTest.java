package com.grahambartley.nightborn.village;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NightMarkupTest {

  @ParameterizedTest(name = "{0}, night {1}, hero {2}: dearer {3}")
  @CsvSource({
    "WARY, true, false, true",
    "HUNTED, true, false, true",
    "CALM, true, false, false",
    "WARY, false, false, false",
    "WARY, true, true, false"
  })
  @DisplayName("a wary village charges strangers more after dark, never its hero")
  void applies(
      final VillageStep step, final boolean night, final boolean hero, final boolean dearer) {
    assertEquals(dearer, NightMarkup.applies(step, night, hero));
  }

  @ParameterizedTest(name = "price {0} at {1}% adds {2}")
  @CsvSource({"8, 25, 2", "1, 25, 1", "10, 25, 3", "64, 50, 32", "8, 0, 0", "0, 25, 0"})
  @DisplayName("the markup is a share of the price, rounded up, and at least one")
  void added(final int price, final int percent, final int added) {
    assertEquals(added, NightMarkup.added(price, percent));
  }
}
