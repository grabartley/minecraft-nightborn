package com.grahambartley.nightborn.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SkillTooltipBoxTest {

  @ParameterizedTest(name = "mouse {0},{1} on {4}x{5}")
  @CsvSource({
    "10, 10, 120, 6, 320, 240",
    "300, 10, 120, 6, 320, 240",
    "160, 235, 120, 6, 320, 240",
    "470, 265, 150, 6, 480, 270",
    "5, 120, 300, 1, 320, 240"
  })
  @DisplayName("the tooltip always lands fully on screen, whatever corner the mouse is in")
  void staysOnScreen(
      final int mouseX,
      final int mouseY,
      final int contentWidth,
      final int lines,
      final int width,
      final int height) {
    final SkillTooltipBox box =
        SkillTooltipBox.around(mouseX, mouseY, contentWidth, lines, width, height);
    assertTrue(box.x() >= 0 && box.x() + box.width() <= width, "x " + box);
    assertTrue(box.y() >= 0 && box.y() + box.height() <= height, "y " + box);
  }

  @ParameterizedTest(name = "mouse at x {0}")
  @CsvSource({"20, true", "150, true", "250, false"})
  @DisplayName("the tooltip sits right of the cursor, and flips left when it would run off")
  void flips(final int mouseX, final boolean right) {
    final SkillTooltipBox box = SkillTooltipBox.around(mouseX, 100, 80, 3, 320, 240);
    assertEquals(right, box.x() > mouseX, box.toString());
    assertTrue(box.x() > mouseX || box.x() + box.width() < mouseX, "never under the cursor");
  }

  @ParameterizedTest(name = "{0} lines")
  @CsvSource({"1, 9", "2, 21", "6, 61"})
  @DisplayName("the box is tall enough for every line, with a gap under the title")
  void height(final int lines, final int content) {
    assertEquals(content, SkillTooltipBox.contentHeight(lines));
    final SkillTooltipBox box = SkillTooltipBox.around(50, 50, 40, lines, 320, 240);
    assertEquals(content + SkillTooltipBox.PAD * 2, box.height());
    assertTrue(box.lineY(lines - 1) + 9 <= box.y() + box.height() - SkillTooltipBox.PAD + 1);
  }
}
