package com.grahambartley.nightborn.mob.household;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NightPhaseTest {

  @ParameterizedTest(name = "fixed {0}, time {1}: {2}, lights {3}, chores {4}")
  @CsvSource({
    "false, 6000, DAY, false, false",
    "false, 12999, DAY, false, false",
    "false, 13000, GATHER, true, true",
    "false, 14499, GATHER, true, true",
    "false, 14500, PASTIME, true, false",
    "false, 20999, PASTIME, true, false",
    "false, 21000, WIND_DOWN, false, true",
    "false, 22999, WIND_DOWN, false, true",
    "false, 23000, DAY, false, false",
    "false, 37000, GATHER, true, true",
    "true, 6000, PASTIME, true, false"
  })
  @DisplayName(
      "a household gathers at dusk, keeps its hobbies through the night and closes up before dawn")
  void phase(
      final boolean fixedTime,
      final long time,
      final NightPhase phase,
      final boolean lights,
      final boolean chores) {
    assertEquals(phase, NightPhase.of(fixedTime, time));
    assertEquals(lights, phase.lightsWanted());
    assertEquals(chores, phase.choresDue());
  }
}
