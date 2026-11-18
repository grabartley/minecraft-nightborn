package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HexMarkTest {

  @ParameterizedTest(name = "heal {0}, share {1}, until {2}, now {3}: {4}")
  @CsvSource({"4, 0.5, 400, 100, 2", "4, 0.5, 400, 400, 4", "6, 1.0, 400, 0, 6"})
  @DisplayName("a hexed creature heals by the hex's share until it lifts")
  void heals(
      final float amount, final float share, final long until, final long now, final float healed) {
    assertEquals(healed, new HexMark(UUID.randomUUID(), share, until).heal(amount, now), 1.0E-6);
  }
}
