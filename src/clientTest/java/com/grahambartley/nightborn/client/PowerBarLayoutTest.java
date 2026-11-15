package com.grahambartley.nightborn.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PowerBarLayoutTest {

  @ParameterizedTest(name = "{0}x{1}, {2} slots, offhand right {3}")
  @CsvSource({
    "320, 240, 1, false",
    "320, 240, 5, false",
    "427, 240, 5, false",
    "640, 360, 5, true",
    "1920, 1080, 5, true",
    "480, 270, 3, true"
  })
  @DisplayName("every slot is on screen, clear of the hotbar and of each other")
  void fits(final int width, final int height, final int count, final boolean offhandRight) {
    final List<PowerBarLayout.Point> points =
        PowerBarLayout.slots(width, height, count, offhandRight);
    assertEquals(count, points.size());
    final int hotbarRight = width / 2 + 91 + (offhandRight ? 29 : 0);
    for (int i = 0; i < points.size(); i++) {
      final PowerBarLayout.Point p = points.get(i);
      assertTrue(p.x() >= 0 && p.x() + PowerBarLayout.SLOT <= width, "on screen " + p);
      assertTrue(p.y() >= 0 && p.y() + PowerBarLayout.SLOT <= height, "on screen " + p);
      assertTrue(
          p.x() >= hotbarRight || p.y() + PowerBarLayout.SLOT <= height - 22,
          "clear of hotbar " + p);
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
    final List<PowerBarLayout.Point> points = PowerBarLayout.slots(width, 360, count, false);
    points.forEach(p -> assertEquals(360 - PowerBarLayout.SLOT, p.y()));
  }
}
