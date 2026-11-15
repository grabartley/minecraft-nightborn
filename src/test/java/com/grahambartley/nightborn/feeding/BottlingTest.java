package com.grahambartley.nightborn.feeding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BottlingTest {

  @ParameterizedTest(name = "{0} blood, size {1}, {2} empty: {3} filled, {4} left")
  @CsvSource({
    "2, 4, 5, 0, 2",
    "4, 4, 5, 1, 0",
    "9, 4, 5, 2, 1",
    "9, 4, 1, 1, 5",
    "9, 4, 0, 0, 9",
    "0, 4, 5, 0, 0",
    "3.5, 1, 16, 3, 0.5"
  })
  @DisplayName("bottles fill only from blood actually taken, and only as many as there are empties")
  void fills(
      final float held, final int size, final int empties, final int bottles, final float left) {
    final Bottling.Fill fill = Bottling.fill(held, size, empties);
    assertEquals(bottles, fill.bottles());
    assertEquals(left, fill.leftover(), 0.0001F);
    assertEquals(held, fill.bottles() * size + fill.leftover(), 0.0001F);
  }
}
