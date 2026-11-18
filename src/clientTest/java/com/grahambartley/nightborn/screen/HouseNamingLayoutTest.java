package com.grahambartley.nightborn.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HouseNamingLayoutTest {

  @ParameterizedTest(name = "{0}x{1}")
  @CsvSource({"427, 240, 83, 22", "1920, 1080, 830, 442", "200, 100, 0, 0"})
  @DisplayName("the dialogue sits in the middle of the screen and never off its top left")
  void centres(final int width, final int height, final int left, final int top) {
    final HouseNamingLayout layout = HouseNamingLayout.of(width, height);
    assertEquals(left, layout.left());
    assertEquals(top, layout.top());
  }

  @ParameterizedTest(name = "{0}x{1}")
  @CsvSource({"427, 240", "1920, 1080"})
  @DisplayName("the name, the crest rows, the preview and the button stack without overlapping")
  void stacks(final int width, final int height) {
    final HouseNamingLayout layout = HouseNamingLayout.of(width, height);
    final int bottom = layout.top() + layout.height();
    assertTrue(layout.titleTop() < layout.fieldTop());
    assertTrue(layout.fieldTop() + HouseNamingLayout.FIELD_HEIGHT < layout.problemTop());
    assertTrue(layout.problemTop() + 9 < layout.rowTop(0));
    for (int row = 1; row < HouseNamingLayout.ROWS; row++) {
      assertTrue(layout.rowTop(row - 1) + HouseNamingLayout.ROW_HEIGHT < layout.rowTop(row));
    }
    assertTrue(
        layout.rowTop(HouseNamingLayout.ROWS - 1) + HouseNamingLayout.ROW_HEIGHT
            <= layout.buttonTop());
    assertTrue(layout.previewTop() + HouseNamingLayout.PREVIEW_SIZE <= layout.buttonTop());
    assertTrue(layout.buttonTop() + HouseNamingLayout.BUTTON_HEIGHT <= bottom);
    assertTrue(layout.previewLeft() + HouseNamingLayout.PREVIEW_SIZE < layout.stepBackLeft());
    assertTrue(layout.stepBackLeft() + HouseNamingLayout.STEP_WIDTH < layout.labelCentre());
    assertTrue(layout.labelCentre() < layout.stepOnLeft());
    assertEquals(
        layout.left() + layout.width() - HouseNamingLayout.PAD,
        layout.stepOnLeft() + HouseNamingLayout.STEP_WIDTH);
  }

  @ParameterizedTest(name = "row {0} -> {1}")
  @CsvSource({"-1, 0", "0, 0", "2, 2", "5, 2"})
  @DisplayName("a row outside the three is held to the nearest")
  void rowsClamp(final int row, final int held) {
    final HouseNamingLayout layout = HouseNamingLayout.of(427, 240);
    assertEquals(layout.rowTop(held), layout.rowTop(row));
  }
}
