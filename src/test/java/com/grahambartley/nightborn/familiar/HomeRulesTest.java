package com.grahambartley.nightborn.familiar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HomeRulesTest {

  @ParameterizedTest(name = "working {0}, guard range {1}: stays within {2}")
  @CsvSource({"true, 8, 8", "true, 20, 20", "false, 8, 3", "false, 20, 3"})
  @DisplayName("a familiar roams its guard range by day and keeps close to the coffin at night")
  void range(final boolean working, final int guardRange, final int range) {
    assertEquals(range, HomeRules.range(working, guardRange));
  }

  @ParameterizedTest(name = "creeper {0}, {1} blocks squared from home, range {2}: {3}")
  @CsvSource({
    "false, 0, 8, true",
    "false, 64, 8, true",
    "false, 65, 8, false",
    "true, 0, 8, false",
    "false, 1024, 32, true"
  })
  @DisplayName("a familiar drives off monsters inside its guard range, except creepers")
  void drivesOff(
      final boolean creeper, final double distanceSquared, final int range, final boolean drives) {
    assertEquals(drives, HomeRules.drivesOff(creeper, distanceSquared, range));
  }
}
