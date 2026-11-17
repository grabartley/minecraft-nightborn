package com.grahambartley.nightborn.lord;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BossCrowdTest {

  @ParameterizedTest(name = "held {0}, near {1}, fighting {2}: {3}")
  @CsvSource({
    "1, 3, true, 3",
    "3, 1, true, 3",
    "3, 0, true, 3",
    "3, 1, false, 1",
    "1, 4, false, 4",
    "5, 0, false, 0"
  })
  @DisplayName("a crowd never shrinks in the middle of a fight, and resets once it is over")
  void size(final int held, final int near, final boolean fighting, final int size) {
    assertEquals(size, BossCrowd.size(held, near, fighting));
  }

  @ParameterizedTest(name = "{0} players at {1} percent each: {2}")
  @CsvSource({
    "0, 50, 0.0",
    "1, 50, 0.0",
    "2, 50, 0.5",
    "4, 50, 1.5",
    "3, 0, 0.0",
    "3, 200, 4.0",
    "-2, 50, 0.0"
  })
  @DisplayName("each player beyond the first adds a share of the boss's health")
  void bonus(final int players, final int percent, final double bonus) {
    assertEquals(bonus, BossCrowd.bonus(players, percent), 1.0e-9);
  }

  @ParameterizedTest(name = "health {0}, max {1} to {2}: {3}")
  @CsvSource({
    "300, 300, 450, 450",
    "120, 300, 450, 270",
    "120, 450, 300, 120",
    "300, 300, 300, 300",
    "440, 300, 450, 450"
  })
  @DisplayName("a boss that grows gains the health it grew by, and one that shrinks gains none")
  void healed(final float health, final float before, final float after, final float healed) {
    assertEquals(healed, BossCrowd.healed(health, before, after));
  }
}
