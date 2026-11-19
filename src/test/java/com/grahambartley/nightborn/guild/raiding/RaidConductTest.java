package com.grahambartley.nightborn.guild.raiding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RaidConductTest {

  @ParameterizedTest(name = "same world {0}, alive {1}, spectator {2}, {3} away: chases {4}")
  @CsvSource({
    "true, true, false, 10.0, true",
    "true, true, false, 64.0, true",
    "true, true, false, 64.1, false",
    "false, true, false, 10.0, false",
    "true, false, false, 10.0, false",
    "true, true, true, 10.0, false"
  })
  void chasesAQuarryWithNoHouseOnlyWhenItCanReachThem(
      final boolean sameWorld,
      final boolean alive,
      final boolean spectator,
      final double distance,
      final boolean chases) {
    assertEquals(chases, RaidConduct.chases(sameWorld, alive, spectator, distance));
  }
}
