package com.grahambartley.nightborn.nemesis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NemesisFightsTest {

  static final UUID PLAYER = UUID.fromString("00000000-0000-0000-0000-000000000001");

  @ParameterizedTest(name = "struck before {0}, now {1}: struck {2}")
  @CsvSource({"false, false, false", "false, true, true", "true, false, true", "true, true, true"})
  @DisplayName("a player who struck once in a fight is remembered as the striker until it ends")
  void remembersStriker(final boolean before, final boolean now, final boolean struck) {
    final NemesisFights.Fight fight = new NemesisFights.Fight(null, PLAYER, true, before, 10L);
    final NemesisFights.Fight next = fight.blow(true, now, 50L);
    assertEquals(struck, next.struck());
    assertEquals(50L, next.lastBlow());
  }
}
