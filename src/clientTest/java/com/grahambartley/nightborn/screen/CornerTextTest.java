package com.grahambartley.nightborn.screen;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CornerTextTest {

  @ParameterizedTest(name = "box at {0},{1}, {2} wide with a {3} inset, text {4} px")
  @CsvSource({"0, 0, 22, 3, 19", "100, 40, 22, 3, 13", "-5, 7, 22, 3, 25", "10, 10, 18, 1, 19"})
  @DisplayName("a rank count sits wholly inside the bottom right of the icon, clear of the frame")
  void rank(final int x, final int y, final int size, final int inset, final int textWidth) {
    final int left = CornerText.rightX(x, size, inset, textWidth);
    final int top = CornerText.bottomY(y, size, inset);
    assertTrue(left > x + inset, "starts inside: " + left);
    assertTrue(left + CornerText.scaled(textWidth) < x + size - inset, "ends inside");
    assertTrue(top > y + inset, "below the top: " + top);
    assertTrue(
        top + CornerText.scaled(CornerText.GLYPH_HEIGHT) < y + size - inset, "above the rim");
  }

  @ParameterizedTest(name = "box at {0},{1}, {2} wide with a {3} inset, text {4} px")
  @CsvSource({"0, 0, 22, 3, 5", "100, 40, 22, 3, 11", "-5, 7, 22, 3, 11"})
  @DisplayName("a key label of up to two letters sits wholly inside the top left of the slot")
  void key(final int x, final int y, final int size, final int inset, final int textWidth) {
    final int left = CornerText.leftX(x, inset);
    final int top = CornerText.topY(y, inset);
    assertTrue(left > x + inset, "starts inside: " + left);
    assertTrue(left + CornerText.scaled(textWidth) < x + size - inset, "ends inside");
    assertTrue(top > y + inset, "below the rim: " + top);
    assertTrue(top + CornerText.scaled(CornerText.GLYPH_HEIGHT) < y + size - inset, "inside");
  }
}
