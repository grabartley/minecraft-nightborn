package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RunningPowersTest {

  @ParameterizedTest(name = "boosted {0}, effects {1}, present {2}: running {3}")
  @CsvSource({
    "false, 0, 0, false",
    "true, 0, 0, true",
    "false, 2, 2, true",
    "false, 2, 1, false",
    "false, 1, 0, false",
    "true, 2, 0, true"
  })
  @DisplayName("a power is running while its boost lasts or all its effects are on the mob")
  void running(final boolean boosted, final int effects, final int present, final boolean running) {
    assertEquals(running, RunningPowers.running(boosted, effects, present));
  }
}
