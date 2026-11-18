package com.grahambartley.nightborn.feeding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FeedingWitnessesTest {

  @ParameterizedTest(name = "hunter mob {0}, player {1}, vampire {2} watches: {3}")
  @CsvSource({
    "true, false, false, true",
    "false, true, false, true",
    "false, true, true, false",
    "false, false, false, false",
    "false, false, true, false"
  })
  void hunterMobsAndHumanPlayersWatch(
      final boolean hunter, final boolean player, final boolean vampire, final boolean watches) {
    assertEquals(watches, FeedingWitnesses.watches(hunter, player, vampire));
  }
}
