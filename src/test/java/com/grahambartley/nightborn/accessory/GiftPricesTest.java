package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GiftPricesTest {

  @ParameterizedTest(name = "{0} at a share of {1}: {2} off")
  @CsvSource({
    "20, 0.95, 1",
    "10, 0.95, 1",
    "8, 0.95, 0",
    "10, 0.9, 1",
    "32, 0.9, 3",
    "10, 1.0, 0",
    "0, 0.9, 0"
  })
  @DisplayName("a price is cut by the wearer's share, rounded")
  void cut(final int price, final double share, final int off) {
    assertEquals(off, GiftPrices.cut(price, share));
  }
}
