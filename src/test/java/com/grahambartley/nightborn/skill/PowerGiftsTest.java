package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PowerGiftsTest {

  @ParameterizedTest(name = "cost {0}, bat form {1}, cut {2}: {3}")
  @CsvSource({
    "3, true, -1.0, 2",
    "1, true, -1.0, 1",
    "2, true, -4.0, 1",
    "3, false, -1.0, 3",
    "0, true, -1.0, 0",
    "3, true, 0.0, 3"
  })
  @DisplayName("bat form costs less with the gift, never less than 1")
  void blood(final int cost, final boolean batForm, final double cut, final int paid) {
    assertEquals(paid, PowerGifts.blood(cost, batForm, cut));
  }

  @ParameterizedTest(name = "general {0}, tracking {1}, share {2}: {3}")
  @CsvSource({"1.0, true, 0.8, 0.8", "1.0, false, 0.8, 1.0", "0.92, true, 0.8, 0.736"})
  @DisplayName("tracking's own cooldown share applies only to tracking")
  void cooldown(
      final double general, final boolean tracking, final double share, final double factor) {
    assertEquals(factor, PowerGifts.cooldown(general, tracking, share), 1.0E-9);
  }
}
