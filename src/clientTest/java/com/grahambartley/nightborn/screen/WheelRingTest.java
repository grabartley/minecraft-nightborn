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

  @ParameterizedTest(name = "{0} entries, hovered {1} -> {2}")
  @CsvSource({"1, -1, false", "1, 0, true", "2, -1, true", "6, 3, true", "0, -1, false"})
  @DisplayName("spokes and glow are drawn only when there is more than one entry or one is hovered")
  void drawsOverlay(final int count, final int hovered, final boolean draws) {
    assertEquals(draws, WheelRing.drawsOverlay(count, hovered));
  }

  @ParameterizedTest(name = "{0} entries -> spokes {1}")
  @CsvSource({"0, false", "1, false", "2, true", "8, true"})
  @DisplayName("a single entry fills the ring, so only two or more are split by spokes")
  void hasSpokes(final int count, final boolean spokes) {
    assertEquals(spokes, WheelRing.hasSpokes(count));
  }

  @ParameterizedTest(name = "entry {0} of {1}")
  @CsvSource({"0, 2", "1, 2", "0, 4", "3, 4", "5, 8"})
  @DisplayName("each spoke fills the gap just before its entry, touching the previous entry's end")
  void spokeStart(final int index, final int count) {
    final float spoke = WheelRing.spokeStartDegrees(index, count);
    assertEquals(WheelRing.startDegrees(index, count) - WheelRing.GAP_DEGREES, spoke, 1.0E-4F);
    final int previous = (index + count - 1) % count;
    final float previousEnd = WheelRing.endDegrees(previous, count);
    final float gap =
        ((WheelRing.startDegrees(index, count) - previousEnd) % 360.0F + 360.0F) % 360.0F;
    assertEquals(WheelRing.GAP_DEGREES, gap, 1.0E-3F);
  }

  @ParameterizedTest(name = "rim {0}")
  @ValueSource(ints = {WheelRing.RIM})
  @DisplayName("the glow sits inside the ring's gilt rims")
  void glowInsideRims(final int rim) {
    assertEquals(WheelRing.INNER + rim, WheelRing.glowInner());
    assertEquals(WheelRing.OUTER - rim, WheelRing.glowOuter());
    assertTrue(WheelRing.glowInner() < WheelRing.glowOuter());
  }
}
