package com.grahambartley.nightborn.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ExperienceBarTest {

  @ParameterizedTest(name = "{0} of the way: {1} px")
  @CsvSource({"0, 0", "0.5, 59", "1, 118", "1.4, 118", "-0.2, 0"})
  @DisplayName("the fill grows with the experience banked and stays inside the frame")
  void filled(final float fraction, final int pixels) {
    assertEquals(pixels, new ExperienceBar(0, 0, 120).filled(fraction));
  }

  @ParameterizedTest(name = "{0} shown as {1}")
  @CsvSource({"0, 0", "37.9, 37", "99.99, 99", "-2, 0"})
  @DisplayName("banked experience is shown as a whole number, rounded down")
  void shown(final float experience, final int shown) {
    assertEquals(shown, ExperienceBar.shown(experience));
  }

  @ParameterizedTest(name = "mouse at {0},{1}: over {2}")
  @CsvSource({"40, 50, true", "159, 58, true", "160, 55, false", "39, 55, false", "80, 59, false"})
  @DisplayName("the bar knows when the mouse is over it")
  void contains(final int mouseX, final int mouseY, final boolean over) {
    assertEquals(over, new ExperienceBar(40, 50, 120).contains(mouseX, mouseY));
  }

  @ParameterizedTest(name = "detail {0} px wide")
  @CsvSource({"90", "140"})
  @DisplayName("the bar sits under the earned and spent line, inside the points row")
  void underTheDetail(final int width) {
    final SkillPointsRow row = SkillPointsRow.centred(200, 12, width);
    final ExperienceBar bar = ExperienceBar.under(row, 20, width);
    assertEquals(row.detailX(), bar.left());
    assertEquals(row.iconX() + row.width(), bar.right());
    assertTrue(bar.top() >= 20 + 9, "below the line of text");
    assertTrue(bar.bottom() <= 20 + SkillPointsRow.HEIGHT, "inside the row");
    assertEquals(bar.left() + width / 2, bar.centre());
  }
}
