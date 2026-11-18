package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StrikeHealingTest {

  @ParameterizedTest(name = "heal {0}, at {1}, last {2}, every {3}: {4}")
  @CsvSource({
    "1.0, 100, -9223372036854775808, 40, true",
    "1.0, 100, 60, 40, true",
    "1.0, 100, 61, 40, false",
    "1.0, 100, 100, 40, false",
    "0.0, 100, -9223372036854775808, 40, false",
    "1.0, 100, 99, 0, true"
  })
  @DisplayName("a blow heals once each interval, and only with the gift")
  void ready(
      final double heal, final long now, final long last, final double every, final boolean ready) {
    assertEquals(ready, StrikeHealing.ready(heal, now, last, every));
  }
}
