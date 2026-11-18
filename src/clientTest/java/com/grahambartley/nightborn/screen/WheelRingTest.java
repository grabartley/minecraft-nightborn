package com.grahambartley.nightborn.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class WheelRingTest {

  @ParameterizedTest(name = "({0}, {1}) of {2} -> {3}")
  @CsvSource({
    "0, -66, 4, 0",
    "66, 0, 4, 1",
    "0, 66, 4, 2",
    "-66, 0, 4, 3",
    "0, 0, 4, -1",
    "0, -35, 4, -1",
    "0, -37, 4, 0",
    "0, -96, 4, 0",
    "0, -97, 4, -1",
    "10, -66, 1, 0",
    "0, -66, 0, -1",
    "46, -46, 8, 1",
    "-46, -46, 8, 7"
  })
  @DisplayName("the sector under the pointer counts clockwise from the top, and the hub is none")
  void sectorAt(final double dx, final double dy, final int count, final int sector) {
    assertEquals(sector, WheelRing.sectorAt(dx, dy, count));
  }

  @ParameterizedTest(name = "{0} sectors")
  @ValueSource(ints = {1, 2, 3, 7, 16})
  @DisplayName("each icon sits in the middle of the sector the pointer picks")
  void iconsSitInTheirSector(final int count) {
    for (int index = 0; index < count; index++) {
      assertEquals(
          index,
          WheelRing.sectorAt(WheelRing.iconX(index, count), WheelRing.iconY(index, count), count));
      assertTrue(WheelRing.endDegrees(index, count) > WheelRing.startDegrees(index, count));
    }
  }

  @ParameterizedTest(name = "key {0} of {1} -> {2}")
  @CsvSource({"0, 3, 0", "2, 3, 2", "3, 3, -1", "-1, 3, -1"})
  @DisplayName("number keys pick the sectors in order and nothing past the last")
  void numberKey(final int key, final int count, final int sector) {
    assertEquals(sector, WheelRing.numberKey(key, count));
  }
}
