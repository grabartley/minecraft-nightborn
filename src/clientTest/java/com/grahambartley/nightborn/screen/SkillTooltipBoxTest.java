package com.grahambartley.nightborn.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
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
        SkillTooltipBox.around(
            mouseX, mouseY, Collections.nCopies(lines, contentWidth), 1, width, height);
    assertTrue(box.x() >= 0 && box.x() + box.width() <= width, "x " + box);
    assertTrue(box.y() >= 0 && box.y() + box.height() <= height, "y " + box);
  }

  @ParameterizedTest(name = "mouse at x {0}")
  @CsvSource({"20, true", "150, true", "250, false"})
  @DisplayName("the tooltip sits right of the cursor, and flips left when it would run off")
  void flips(final int mouseX, final boolean right) {
    final SkillTooltipBox box =
        SkillTooltipBox.around(mouseX, 100, List.of(40, 80, 60), 1, 320, 240);
    assertEquals(right, box.x() > mouseX, box.toString());
    assertTrue(box.x() > mouseX || box.x() + box.width() < mouseX, "never under the cursor");
  }

  @ParameterizedTest(name = "{0} lines")
  @CsvSource({"1, 9", "2, 21", "6, 61"})
  @DisplayName("the box is tall enough for every line, with a gap under the title")
  void height(final int lines, final int content) {
    assertEquals(content, SkillTooltipBox.contentHeight(lines, 1));
    final SkillTooltipBox box =
        SkillTooltipBox.around(50, 50, Collections.nCopies(lines, 40), 1, 320, 240);
    assertEquals(content + SkillTooltipBox.PAD * 2, box.height());
    assertTrue(box.lineY(lines - 1) + 9 <= box.y() + box.height() - SkillTooltipBox.PAD + 1);
  }

  @ParameterizedTest(name = "widths {0}, {1}, {2}")
  @CsvSource({"40, 120, 60", "120, 40, 60", "60, 40, 120"})
  @DisplayName("the box is as wide as the widest line, wherever it falls")
  void widestLine(final int first, final int second, final int third) {
    final SkillTooltipBox box =
        SkillTooltipBox.around(10, 10, List.of(first, second, third), 1, 320, 240);
    assertEquals(120 + SkillTooltipBox.PAD * 2, box.width());
  }

  @ParameterizedTest(name = "screen {0} wide")
  @CsvSource({"1920", "427", "320", "180", "20"})
  @DisplayName("tooltip lines wrap so the widest box still fits the screen")
  void wraps(final int screenWidth) {
    final int wrap = SkillTooltipBox.wrapWidth(screenWidth);
    assertTrue(wrap <= SkillTooltipBox.MAX_WIDTH, "never wider than a readable line");
    final SkillTooltipBox box =
        SkillTooltipBox.around(screenWidth / 2, 50, List.of(wrap, wrap / 2), 1, screenWidth, 240);
    assertTrue(
        screenWidth < SkillTooltipBox.MIN_WRAP + (SkillTooltipBox.EDGE + SkillTooltipBox.PAD) * 2
            || box.x() >= SkillTooltipBox.EDGE
                && box.x() + box.width() <= screenWidth - SkillTooltipBox.EDGE,
        "box from " + box.x() + " to " + (box.x() + box.width()));
  }

  @ParameterizedTest(name = "{0} lines, the first {1} of them the title")
  @CsvSource({
    "1, 0, 0, 10, 20",
    "3, 0, 0, 10, 20",
    "3, 1, 0, 12, 22",
    "4, 2, 0, 10, 22",
    "5, 2, 0, 10, 22"
  })
  @DisplayName("the gap falls under the whole title, and a tooltip with no body has none")
  void titleGap(
      final int lines, final int titleLines, final int first, final int second, final int third) {
    final SkillTooltipBox box =
        SkillTooltipBox.around(50, 50, Collections.nCopies(lines, 40), titleLines, 320, 240);
    final int top = box.y() + SkillTooltipBox.PAD;
    assertEquals(top + first, box.lineY(0));
    assertEquals(top + second, box.lineY(1));
    if (lines > 2) {
      assertEquals(top + third, box.lineY(2));
    }
    assertTrue(
        box.lineY(lines - 1) + SkillTooltipBox.LINE - 1
            == box.y() + box.height() - SkillTooltipBox.PAD,
        "the last line ends at the bottom padding");
  }
}
