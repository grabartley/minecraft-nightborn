package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GiftScaleTest {

  @ParameterizedTest(name = "{0} ticks x {1} = {2}")
  @CsvSource({
    "200, 1.0, 200",
    "200, 0.5, 100",
    "200, 1.25, 250",
    "200, 0.7, 140",
    "1, 0.2, 1",
    "0, 0.5, 0",
    "100, -1.0, 1"
  })
  @DisplayName("a time is scaled and never drops to nothing")
  void ticks(final int ticks, final double factor, final int scaled) {
    assertEquals(scaled, GiftScale.ticks(ticks, factor));
  }

  @ParameterizedTest(name = "{0} x {1} = {2}")
  @CsvSource({"10.0, 1.0, 10.0", "10.0, 0.5, 5.0", "8.0, 0.75, 6.0", "4.0, -1.0, 0.0"})
  @DisplayName("an amount is scaled and never turns negative")
  void amount(final float amount, final double factor, final float scaled) {
    assertEquals(scaled, GiftScale.amount(amount, factor), 1.0E-4F);
  }

  @ParameterizedTest(name = "{0} x {1} = {2}")
  @CsvSource({"10, 0.8, 8", "5, 0.8, 4", "1, 0.8, 1", "0, 0.8, 0", "10, 1.0, 10"})
  @DisplayName("a count is rounded to the nearest whole")
  void count(final int count, final double factor, final int scaled) {
    assertEquals(scaled, GiftScale.count(count, factor));
  }
}
