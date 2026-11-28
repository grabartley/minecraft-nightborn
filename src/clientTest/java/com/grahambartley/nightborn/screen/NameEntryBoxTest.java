package com.grahambartley.nightborn.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NameEntryBoxTest {

  private record Box(int left, int top, int width, int height) implements NameEntryBox {

    @Override
    public int buttonTop() {
      return top + height;
    }
  }

  @ParameterizedTest(name = "{1} wide in {0}")
  @CsvSource({"427, 220, 103", "1920, 260, 830", "200, 220, 0", "220, 220, 0"})
  @DisplayName("a box sits in the middle of the screen and never off its edge")
  void centred(final int screen, final int size, final int start) {
    assertEquals(start, NameEntryBox.centred(screen, size));
  }

  @ParameterizedTest(name = "box at {0},{1} sized {2}x{3}")
  @CsvSource({
    "0, 0, 220, 110, 110, 10, 10, 200, 34, 60, 62",
    "103, 65, 220, 110, 213, 75, 113, 200, 99, 125, 165",
    "40, 20, 260, 196, 170, 30, 50, 240, 54, 80, 122"
  })
  @DisplayName("the title, field, message and button are placed from the box alone")
  void places(
      final int left,
      final int top,
      final int width,
      final int height,
      final int centre,
      final int titleTop,
      final int fieldLeft,
      final int fieldWidth,
      final int fieldTop,
      final int problemTop,
      final int buttonLeft) {
    final NameEntryBox box = new Box(left, top, width, height);
    assertEquals(centre, box.centre());
    assertEquals(titleTop, box.titleTop());
    assertEquals(fieldLeft, box.fieldLeft());
    assertEquals(fieldWidth, box.fieldWidth());
    assertEquals(fieldTop, box.fieldTop());
    assertEquals(problemTop, box.problemTop());
    assertEquals(buttonLeft, box.buttonLeft());
  }
}
