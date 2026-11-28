package com.grahambartley.nightborn.guild.veteran;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VeteranDodgeTest {

  @ParameterizedTest(name = "moved {0}: dodged {1}")
  @CsvSource({"0.5, false", "3.0, false", "3.1, true", "12.0, true"})
  void countsATeleportDuringTheWindUpAsADodge(final double moved, final boolean dodged) {
    assertEquals(dodged, VeteranDodge.dodged(moved));
  }

  @ParameterizedTest(name = "dodged {0}, alive {1}, vanished {2}, sees {3}, {4} of {5}: lands {6}")
  @CsvSource({
    "false, true, false, true, 6.0, 12.0, true",
    "true, true, false, true, 6.0, 12.0, false",
    "false, false, false, true, 6.0, 12.0, false",
    "false, true, true, true, 6.0, 12.0, false",
    "false, true, false, false, 6.0, 12.0, false",
    "false, true, false, true, 12.5, 12.0, false"
  })
  void landsTheChainOnlyOnAFoeStillInSightAndReach(
      final boolean dodged,
      final boolean alive,
      final boolean vanished,
      final boolean sees,
      final double distance,
      final double reach,
      final boolean lands) {
    assertEquals(lands, VeteranDodge.chainLands(dodged, alive, vanished, sees, distance, reach));
  }
}
