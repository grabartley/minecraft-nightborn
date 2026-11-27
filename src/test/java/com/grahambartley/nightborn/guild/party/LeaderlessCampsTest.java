package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LeaderlessCampsTest {

  @ParameterizedTest(name = "stay over {0}, chunks loaded {1}, leader here {2}: cleared {3}")
  @CsvSource({
    "true, true, false, true",
    "true, true, true, false",
    "true, false, false, false",
    "false, true, false, false",
    "false, false, true, false"
  })
  void clearsOnlyALoadedCampWithNoLeaderWhenItsNightsRunOut(
      final boolean over, final boolean loaded, final boolean led, final boolean cleared) {
    assertEquals(cleared, LeaderlessCamps.clears(over, loaded, led));
  }
}
