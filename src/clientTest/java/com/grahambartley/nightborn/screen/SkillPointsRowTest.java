package com.grahambartley.nightborn.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SkillPointsRowTest {

  @ParameterizedTest(name = "count {1} px, detail {2} px")
  @CsvSource({"160, 5, 90", "240, 11, 120", "100, 17, 0"})
  @DisplayName(
      "the point graphic, the big count and the earned and spent line sit in order, centred")
  void inOrder(final int centre, final int count, final int detail) {
    final SkillPointsRow row = SkillPointsRow.centred(centre, count, detail);
    assertTrue(row.iconX() + SkillPointsRow.ICON <= row.countX());
    assertTrue(row.countX() + count * SkillPointsRow.COUNT_SCALE <= row.detailX());
    assertEquals(row.iconX() + row.width(), row.detailX() + detail);
    assertTrue(Math.abs(row.iconX() + row.width() / 2 - centre) <= 1, "centred");
  }

  @ParameterizedTest(name = "mouse at {0},{1}")
  @CsvSource({
    "100, 30, true",
    "118, 40, true",
    "124, 35, true",
    "99, 30, false",
    "100, 48, true",
    "100, 49, false",
    "150, 35, false"
  })
  @DisplayName("hovering the graphic or the count names what the number means")
  void hover(final int mouseX, final int mouseY, final boolean over) {
    final SkillPointsRow row = new SkillPointsRow(100, 120, 140, 100);
    assertEquals(over, row.overPoints(30, mouseX, mouseY, 6));
  }
}
