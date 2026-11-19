package com.grahambartley.nightborn.guild.raiding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RaidConductTest {

  @ParameterizedTest(
      name = "same world {0}, alive {1}, spectator {2}, {3} away, chase {4}: chases {5}")
  @CsvSource({
    "true, true, false, 10.0, 64, true",
    "true, true, false, 64.0, 64, true",
    "true, true, false, 64.1, 64, false",
    "true, true, false, 40.0, 32, false",
    "false, true, false, 10.0, 64, false",
    "true, false, false, 10.0, 64, false",
    "true, true, true, 10.0, 64, false"
  })
  void chasesAQuarryWithNoHouseOnlyWhenItCanReachThem(
      final boolean sameWorld,
      final boolean alive,
      final boolean spectator,
      final double distance,
      final int chase,
      final boolean chases) {
    assertEquals(chases, RaidConduct.chases(sameWorld, alive, spectator, distance, chase));
  }
}
