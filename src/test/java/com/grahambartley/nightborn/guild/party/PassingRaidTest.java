package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PassingRaidTest {

  @ParameterizedTest(name = "pitched {0}, night {1}: looks {2}")
  @CsvSource({
    "false, false, true",
    "true, false, false",
    "false, true, false",
    "true, true, false"
  })
  void looksForHousesOnlyWhileOnTheMoveByDay(
      final boolean pitched, final boolean night, final boolean looks) {
    assertEquals(looks, PassingRaid.looks(pitched, night));
  }

  @ParameterizedTest(
      name = "same world {0}, {1} away, online {2}, vampire {3}, quiet {4}, underway {5}: {6}")
  @CsvSource({
    "true, 20.0, true, true, false, false, true",
    "true, 32.0, true, true, false, false, true",
    "true, 32.5, true, true, false, false, false",
    "false, 20.0, true, true, false, false, false",
    "true, 20.0, false, false, false, false, false",
    "true, 20.0, true, false, false, false, false",
    "true, 20.0, true, true, true, false, false",
    "true, 20.0, true, true, false, true, false"
  })
  void raidsAHouseInSightOfAVampireWhoIsNotResting(
      final boolean sameWorld,
      final double distance,
      final boolean online,
      final boolean vampire,
      final boolean quiet,
      final boolean underway,
      final boolean raids) {
    assertEquals(raids, PassingRaid.targets(sameWorld, distance, online, vampire, quiet, underway));
  }
}
