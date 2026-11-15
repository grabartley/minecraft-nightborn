package com.grahambartley.nightborn.feeding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VillagerWitnessesTest {

  @ParameterizedTest(name = "victim {0}, alive {1}, asleep {2}, dist² {3}, sight {4}: sees {5}")
  @CsvSource({
    "false, true, false, 100, true, true",
    "true, true, false, 100, true, false",
    "false, false, false, 100, true, false",
    "false, true, true, 100, true, false",
    "false, true, false, 256, true, true",
    "false, true, false, 257, true, false",
    "false, true, false, 100, false, false"
  })
  @DisplayName("only an awake villager in range with line of sight is a witness")
  void sees(
      final boolean victim,
      final boolean alive,
      final boolean asleep,
      final double distanceSquared,
      final boolean sight,
      final boolean sees) {
    assertEquals(
        sees, VillagerWitnesses.sees(victim, alive, asleep, distanceSquared, 16, () -> sight));
  }
}
