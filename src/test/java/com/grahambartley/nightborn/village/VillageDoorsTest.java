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

  @ParameterizedTest(name = "asleep {0}, {1} blocks² from home: sent home {2}")
  @CsvSource({"false, 100, true", "false, 9, false", "true, 100, false", "false, 10, true"})
  @DisplayName("villagers out of doors at night are sent home")
  void sendsHome(final boolean sleeping, final double distance, final boolean home) {
    assertEquals(home, VillageDoors.sendsHome(sleeping, distance));
  }
}
