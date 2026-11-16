package com.grahambartley.nightborn.familiar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.OptionalLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WagesTest {

  @ParameterizedTest(name = "paid through {0} on day {1}, {2} per emerald, {3} ahead: {4}")
  @CsvSource({
    "10, 10, 1, 7, 11",
    "4, 10, 1, 7, 10",
    "4, 10, 3, 7, 12",
    "15, 10, 1, 7, 16",
    "15, 10, 5, 7, 16",
    "16, 10, 1, 7, -1",
    "20, 10, 1, 7, -1",
    "9, 10, 1, 1, 10",
    "10, 10, 1, 1, -1"
  })
  @DisplayName("an emerald pays days from today on, up to the days-ahead limit")
  void pay(
      final long paidThrough,
      final long today,
      final int perEmerald,
      final int ahead,
      final long expected) {
    final OptionalLong paid = Wages.pay(paidThrough, today, perEmerald, ahead);
    assertEquals(expected < 0 ? OptionalLong.empty() : OptionalLong.of(expected), paid);
  }

  @ParameterizedTest(name = "paid through {0}, day {1}: paid {2}")
  @CsvSource({"10, 9, true", "10, 10, true", "10, 11, false"})
  @DisplayName("a day is paid for when it falls on or before the paid-through day")
  void paidFor(final long paidThrough, final long day, final boolean paid) {
    assertEquals(paid, Wages.paidFor(paidThrough, day));
  }
}
