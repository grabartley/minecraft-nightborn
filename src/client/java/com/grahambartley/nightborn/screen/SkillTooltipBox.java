package com.grahambartley.nightborn.screen;

import java.util.List;

public record SkillTooltipBox(int x, int y, int width, int height, int titleLines) {

  static final int PAD = 6;
  static final int LINE = 10;
  static final int TITLE_GAP = 2;
  static final int CURSOR_GAP = 12;
  static final int EDGE = 2;
  static final int MAX_WIDTH = 200;
  static final int MIN_WRAP = 40;

  public static int contentHeight(final int lines, final int titleLines) {
    if (lines <= 0) {
      return 0;
    }
    return LINE * lines - 1 + (gapped(lines, titleLines) ? TITLE_GAP : 0);
  }

  private static boolean gapped(final int lines, final int titleLines) {
    return titleLines > 0 && titleLines < lines;
  }

  public static int wrapWidth(final int screenWidth) {
    return Math.max(MIN_WRAP, Math.min(MAX_WIDTH, screenWidth - (EDGE + PAD) * 2));
  }

  public static SkillTooltipBox around(
      final int mouseX,
      final int mouseY,
      final List<Integer> lineWidths,
      final int titleLines,
      final int screenWidth,
      final int screenHeight) {
    final int contentWidth = lineWidths.stream().mapToInt(Integer::intValue).max().orElse(0);
    final int width = contentWidth + PAD * 2;
    final int height = contentHeight(lineWidths.size(), titleLines) + PAD * 2;
    int x = mouseX + CURSOR_GAP;
    if (x + width > screenWidth - EDGE) {
      x = mouseX - CURSOR_GAP - width;
    }
    x = clamp(x, EDGE, screenWidth - EDGE - width);
    final int y = clamp(mouseY - CURSOR_GAP, EDGE, screenHeight - EDGE - height);
    return new SkillTooltipBox(x, y, width, height, titleLines);
  }

  public int lineY(final int index) {
    return y + PAD + index * LINE + (titleLines > 0 && index >= titleLines ? TITLE_GAP : 0);
  }

  private static int clamp(final int value, final int min, final int max) {
    return Math.max(min, Math.min(value, Math.max(min, max)));
  }
}
