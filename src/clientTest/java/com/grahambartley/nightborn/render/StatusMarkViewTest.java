package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StatusMarkViewTest {

  private static final float EPSILON = 1.0E-5F;

  @ParameterizedTest(name = "invisible {0}, until {1}, now {2}: shown {3}")
  @CsvSource({
    "false, 200, 100, true",
    "false, 200, 200, false",
    "false, , 100, false",
    "true, 200, 100, false"
  })
  @DisplayName("a mark shows while its state lasts, and never on an unseen body")
  void shows(final boolean invisible, final Long until, final long now, final boolean shown) {
    assertEquals(shown, StatusMarkView.shows(invisible, until, now));
  }

  @ParameterizedTest(name = "body {0} by {1}: net {2} by {3}")
  @CsvSource({"0.6, 1.8, 0.69, 1.6941177", "0.5, 0.9, 0.575, 0.84705883", "1.2, 3.4, 1.38, 3.2"})
  @DisplayName("the net drapes over the whole body it caught, whatever its size")
  void net(final float width, final float height, final float netWidth, final float netHeight) {
    assertEquals(netWidth, StatusMarkView.netWidth(width), EPSILON);
    assertEquals(netHeight, StatusMarkView.netHeight(height), EPSILON);
  }

  @ParameterizedTest(name = "body {0} by {1}: hex at {2}, scale {3}; brand {4}")
  @CsvSource({
    "0.6, 1.8, 2.1, 1.08, 1.0",
    "0.3, 0.6, 0.9, 0.6, 1.0",
    "2.0, 4.0, 4.3, 1.6, 3.0",
    "1.2, 2.0, 2.3, 1.6, 1.92"
  })
  @DisplayName("the hex hangs over the head and the quarry brand spreads under the feet")
  void marks(
      final float width,
      final float height,
      final float lift,
      final float hexScale,
      final float brand) {
    assertEquals(lift, StatusMarkView.hexLift(height), EPSILON);
    assertEquals(hexScale, StatusMarkView.hexScale(width), EPSILON);
    assertEquals(brand, StatusMarkView.quarryScale(width), EPSILON);
  }
}
