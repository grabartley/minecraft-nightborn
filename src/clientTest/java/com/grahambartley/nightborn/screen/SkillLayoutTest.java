package com.grahambartley.nightborn.screen;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.skill.Skill;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SkillLayoutTest {

  @ParameterizedTest(name = "{0}x{1} with {2} branches")
  @CsvSource({
    "320, 240, 4",
    "427, 240, 4",
    "640, 360, 4",
    "1920, 1080, 4",
    "320, 240, 1",
    "854, 480, 2"
  })
  @DisplayName("every node of every branch fits inside the panel and the panel fits the screen")
  void fits(final int width, final int height, final int branches) {
    final SkillLayout layout = SkillLayout.of(width, height, branches);
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
    final int lastRow = layout.nodeY(Skill.MAX_ROW) + SkillLayout.NODE;
    assertTrue(lastRow <= layout.top() + layout.height(), "last row inside the panel");
  }

  @ParameterizedTest(name = "columns {0} and {1}")
  @CsvSource({"0, 1", "1, 2"})
  @DisplayName("neighbouring nodes never overlap at the smallest screen")
  void noOverlap(final int left, final int right) {
    final SkillLayout layout = SkillLayout.of(320, 240, 4);
    assertTrue(layout.nodeX(0, 4, left) + SkillLayout.NODE <= layout.nodeX(0, 4, right));
    assertTrue(layout.nodeY(0) + SkillLayout.NODE < layout.nodeY(1));
  }

  @ParameterizedTest(name = "mouse at {0},{1} -> {2}")
  @CsvSource({"10, 10, true", "31, 31, true", "32, 10, false", "9, 10, false", "10, 32, false"})
  @DisplayName("a node is hovered only inside its square")
  void hover(final double mouseX, final double mouseY, final boolean inside) {
    assertTrue(SkillLayout.contains(10, 10, mouseX, mouseY) == inside);
  }
}
