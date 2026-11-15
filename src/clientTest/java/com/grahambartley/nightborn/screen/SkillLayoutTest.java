package com.grahambartley.nightborn.screen;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.skill.Skill;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SkillLayoutTest {

  @ParameterizedTest(name = "{0}x{1} with {2} branches and {3} rows")
  @CsvSource({
    "320, 240, 4, 6",
    "427, 240, 4, 2",
    "640, 360, 4, 6",
    "1920, 1080, 4, 1",
    "320, 240, 1, 3",
    "854, 480, 2, 6"
  })
  @DisplayName("every node of every branch fits inside the panel and the panel fits the screen")
  void fits(final int width, final int height, final int branches, final int rows) {
    final SkillLayout layout = SkillLayout.of(width, height, branches, rows);
    assertTrue(
        layout.left() >= 0 && layout.left() + layout.width() <= Math.max(width, layout.width()));
    assertTrue(layout.top() + layout.height() <= height, "panel height " + layout.height());
    for (int b = 0; b < branches; b++) {
      for (int column = 0; column <= Skill.MAX_COLUMN; column++) {
        final int x = layout.nodeX(b, branches, column);
        assertTrue(x >= layout.branchLeft(b, branches), "node inside its branch");
        assertTrue(x + SkillLayout.NODE <= layout.branchLeft(b, branches) + layout.branchWidth());
      }
    }
    final int lastRow = layout.nodeY(rows - 1) + SkillLayout.NODE;
    assertTrue(lastRow <= layout.top() + layout.height(), "last row inside the panel");
  }

  @ParameterizedTest(name = "columns {0} and {1}")
  @CsvSource({"0, 1", "1, 2"})
  @DisplayName("neighbouring nodes never overlap at the smallest screen")
  void noOverlap(final int left, final int right) {
    final SkillLayout layout = SkillLayout.of(320, 240, 4, 6);
    assertTrue(layout.nodeX(0, 4, left) + SkillLayout.NODE <= layout.nodeX(0, 4, right));
    assertTrue(layout.nodeY(0) + SkillLayout.NODE < layout.nodeY(1));
  }

  @ParameterizedTest(name = "{0} rows")
  @CsvSource({"1", "2", "6"})
  @DisplayName("the panel grows with the rows in use, so a short tree is not mostly empty")
  void heightFollowsRows(final int rows) {
    final SkillLayout shorter = SkillLayout.of(854, 480, 4, rows);
    final SkillLayout taller = SkillLayout.of(854, 480, 4, rows + 1);
    assertTrue(taller.height() > shorter.height() || rows + 1 > Skill.MAX_ROW + 1);
    assertTrue(
        shorter.nodeY(rows - 1) + SkillLayout.NODE + SkillLayout.PAD
            <= shorter.top() + shorter.height());
  }

  @ParameterizedTest(name = "mouse at {0},{1} -> {2}")
  @CsvSource({"10, 10, true", "31, 31, true", "32, 10, false", "9, 10, false", "10, 32, false"})
  @DisplayName("a node is hovered only inside its square")
  void hover(final double mouseX, final double mouseY, final boolean inside) {
    assertTrue(SkillLayout.contains(10, 10, mouseX, mouseY) == inside);
  }
}
