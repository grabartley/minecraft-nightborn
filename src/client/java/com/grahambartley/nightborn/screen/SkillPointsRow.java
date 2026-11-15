package com.grahambartley.nightborn.screen;

public record SkillPointsRow(int iconX, int countX, int detailX, int width) {

  public static final int ICON = 16;
  public static final int COUNT_SCALE = 2;
  static final int GAP = 4;
  static final int DETAIL_GAP = 10;

  public static SkillPointsRow centred(
      final int centre, final int countTextWidth, final int detailWidth) {
    final int count = countTextWidth * COUNT_SCALE;
    final int width = ICON + GAP + count + DETAIL_GAP + detailWidth;
    final int left = centre - width / 2;
    return new SkillPointsRow(
        left, left + ICON + GAP, left + ICON + GAP + count + DETAIL_GAP, width);
  }

  public boolean overPoints(
      final int top, final double mouseX, final double mouseY, final int countTextWidth) {
    final int right = countX + countTextWidth * COUNT_SCALE;
    return mouseX >= iconX && mouseX < right && mouseY >= top && mouseY < top + ICON;
  }
}
