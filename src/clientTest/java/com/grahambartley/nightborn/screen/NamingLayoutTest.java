package com.grahambartley.nightborn.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NamingLayoutTest {

  @ParameterizedTest(name = "{0}x{1}")
  @CsvSource({"427, 240, 103, 65", "1920, 1080, 850, 485", "200, 100, 0, 0"})
  @DisplayName("the dialogue sits in the middle of the screen and never off its top left")
  void centres(final int width, final int height, final int left, final int top) {
    final NamingLayout layout = NamingLayout.of(width, height);
    assertEquals(left, layout.left());
    assertEquals(top, layout.top());
    assertEquals(NamingLayout.WIDTH, layout.width());
    assertEquals(NamingLayout.HEIGHT, layout.height());
  }

  @ParameterizedTest(name = "{0}x{1}")
  @CsvSource({"427, 240", "1920, 1080"})
  @DisplayName("the field, message and button stack inside the panel without overlapping")
  void stacks(final int width, final int height) {
    final NamingLayout layout = NamingLayout.of(width, height);
    assertTrue(layout.titleTop() < layout.fieldTop());
    assertTrue(layout.fieldTop() + NamingLayout.FIELD_HEIGHT < layout.problemTop());
    assertTrue(layout.problemTop() + 9 < layout.buttonTop());
    assertTrue(layout.buttonTop() + NamingLayout.BUTTON_HEIGHT <= layout.top() + layout.height());
    assertEquals(layout.left() + NamingLayout.PAD, layout.fieldLeft());
    assertEquals(
        layout.left() + layout.width() - NamingLayout.PAD,
        layout.fieldLeft() + layout.fieldWidth());
    assertEquals(layout.centre(), layout.buttonLeft() + NamingLayout.BUTTON_WIDTH / 2);
  }
}
