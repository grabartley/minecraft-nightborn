package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BeltAmmoTest {

  @ParameterizedTest(name = "drew {0}, {1} left: used {2}")
  @CsvSource({"64, 63, 1", "64, 64, 0", "1, 0, 1", "3, -1, 3", "5, 7, 0"})
  @DisplayName("the belt gives up only what loading took from the drawn stack")
  void used(final int before, final int after, final int used) {
    assertEquals(used, BeltAmmo.used(before, after));
  }
}
