package com.grahambartley.nightborn.village;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VillageDoorsTest {

  @ParameterizedTest(name = "{0} at night {1}: doors shut {2}")
  @CsvSource({
    "CALM, true, false",
    "WARY, false, false",
    "WARY, true, true",
    "WARDED, true, true",
    "HUNTED, true, true"
  })
  @DisplayName("a wary village shuts its doors after dark")
  void shutsDoors(final VillageStep step, final boolean night, final boolean shuts) {
    assertEquals(shuts, VillageDoors.shuts(step, night));
  }

  @ParameterizedTest(name = "asleep {0}, busy {1}, {2} blocks² from home: sent home {3}")
  @CsvSource({
    "false, false, 100, true",
    "false, false, 9, false",
    "true, false, 100, false",
    "false, false, 10, true",
    "false, true, 100, false"
  })
  @DisplayName("villagers out at night are sent home, unless running for the bell or fleeing")
  void sendsHome(
      final boolean sleeping, final boolean busy, final double distance, final boolean home) {
    assertEquals(home, VillageDoors.sendsHome(sleeping, busy, distance));
  }

  @ParameterizedTest(name = "night {0}, last shut on {1}: shuts again {2}")
  @CsvSource({"4, , true", "4, 3, true", "4, 4, false"})
  @DisplayName("doors are shut once each night, so a door opened later stays open")
  void shutsOnceANight(final long night, final Long lastShut, final boolean shuts) {
    assertEquals(shuts, VillageDoors.shutsTonight(night, lastShut));
  }
}
