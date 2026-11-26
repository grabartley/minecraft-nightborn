package com.grahambartley.nightborn.screen;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CornerTextTest {

  @ParameterizedTest(name = "box at {0},{1}, {2} wide with a {3} inset, text {4} px")
  @CsvSource({"0, 0, 22, 3, 19", "100, 40, 22, 3, 13", "-5, 7, 22, 3, 25", "10, 10, 18, 1, 19"})
  @DisplayName("corner text sits wholly inside the icon area, clear of the frame")
  void insideTheIcon(
      final int x, final int y, final int size, final int inset, final int textWidth) {
    final int innerLeft = x + inset;
    final int innerTop = y + inset;
    final int innerRight = x + size - inset;
    final int innerBottom = y + size - inset;
    final int width = CornerText.scaled(textWidth);
    final int height = CornerText.scaled(CornerText.GLYPH_HEIGHT);
    final int rightX = CornerText.rightX(x, size, inset, textWidth);
    final int bottomY = CornerText.bottomY(y, size, inset);
    assertTrue(rightX > innerLeft && rightX + width < innerRight, "rank inside: " + rightX);
    assertTrue(bottomY > innerTop && bottomY + height < innerBottom, "rank inside: " + bottomY);
    final int leftX = CornerText.leftX(x, inset);
    final int topY = CornerText.topY(y, inset);
    assertTrue(leftX > innerLeft && leftX < innerRight, "key inside: " + leftX);
    assertTrue(topY > innerTop && topY + height < innerBottom, "key inside: " + topY);
  }
}
