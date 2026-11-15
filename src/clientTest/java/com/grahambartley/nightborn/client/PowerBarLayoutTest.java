package com.grahambartley.nightborn.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PowerBarLayoutTest {

  static final int HOTBAR_HALF = 91;
  static final int OFFHAND = 29;
  static final int INDICATOR_RIGHT_EDGE = 6 + 18;

  @ParameterizedTest(name = "{0}x{1}, {2} slots, offhand right {3}, indicator right {4}")
  @CsvSource({
    "320, 240, 1, false, false",
    "320, 240, 5, false, true",
    "427, 240, 5, false, false",
    "640, 360, 5, true, false",
    "1920, 1080, 5, true, false",
    "480, 270, 3, true, false",
    "854, 480, 5, false, true"
  })
  @DisplayName(
      "every slot is on screen, clear of the hotbar, offhand, attack indicator and each other")
  void fits(
      final int width,
      final int height,
      final int count,
      final boolean offhandRight,
      final boolean indicatorRight) {
    final List<PowerBarLayout.Point> points =
        PowerBarLayout.slots(width, height, count, offhandRight, indicatorRight);
    assertEquals(count, points.size());
    final int reservedRight =
        width / 2
            + HOTBAR_HALF
            + (offhandRight ? OFFHAND : 0)
            + (indicatorRight ? INDICATOR_RIGHT_EDGE : 0);
    for (int i = 0; i < points.size(); i++) {
      final PowerBarLayout.Point p = points.get(i);
      assertTrue(p.x() >= 0 && p.x() + PowerBarLayout.SLOT <= width, "on screen " + p);
      assertTrue(p.y() >= 0 && p.y() + PowerBarLayout.SLOT <= height, "on screen " + p);
      assertTrue(
          p.x() >= reservedRight || p.y() + PowerBarLayout.SLOT <= height - 22,
          "clear of the hotbar row " + p);
      for (int j = 0; j < i; j++) {
        final PowerBarLayout.Point q = points.get(j);
        assertTrue(
            Math.abs(p.x() - q.x()) >= PowerBarLayout.SLOT
                || Math.abs(p.y() - q.y()) >= PowerBarLayout.SLOT);
      }
    }
  }

  @ParameterizedTest(name = "{0} wide")
  @CsvSource({"640, 5", "1920, 5"})
  @DisplayName("on a wide screen the bar runs along the bottom beside the hotbar")
  void besideHotbar(final int width, final int count) {
    PowerBarLayout.slots(width, 360, count, false, false)
        .forEach(p -> assertEquals(360 - PowerBarLayout.SLOT, p.y()));
  }
}
