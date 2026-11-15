package com.grahambartley.nightborn.screen;

import com.grahambartley.nightborn.skill.Skill;

public record SkillLayout(int left, int top, int width, int height, int branchWidth) {

  public static final int NODE = 22;
  static final int ROW_STEP = 30;
  static final int HEADER = 40;
  static final int PAD = 8;
  static final int MAX_BRANCH_WIDTH = 96;
  static final int MIN_BRANCH_WIDTH = NODE * (Skill.MAX_COLUMN + 1);

  public static SkillLayout of(
      final int screenWidth, final int screenHeight, final int branches, final int rows) {
    final int count = Math.max(1, branches);
    final int branchWidth =
        Math.max(MIN_BRANCH_WIDTH, Math.min(MAX_BRANCH_WIDTH, (screenWidth - PAD * 4) / count));
    final int width = Math.max(branchWidth * count + PAD * 2, 200);
    final int height =
        HEADER
            + ROW_STEP * Math.max(1, Math.min(rows, Skill.MAX_ROW + 1))
            - (ROW_STEP - NODE)
            + PAD * 2;
    return new SkillLayout(
        Math.max(0, (screenWidth - width) / 2),
        Math.max(0, (screenHeight - height) / 2),
        width,
        height,
        branchWidth);
  }

  public int branchLeft(final int index, final int branches) {
    final int used = branchWidth * Math.max(1, branches);
    return left + (width - used) / 2 + index * branchWidth;
  }

  public int nodeX(final int branchIndex, final int branches, final int column) {
    final int step = (branchWidth - NODE) / Skill.MAX_COLUMN;
    final int span = step * Skill.MAX_COLUMN + NODE;
    return branchLeft(branchIndex, branches) + (branchWidth - span) / 2 + column * step;
  }

  public int nodeY(final int row) {
    return top + HEADER + row * ROW_STEP;
  }

  public static boolean contains(
      final int x, final int y, final double mouseX, final double mouseY) {
    return mouseX >= x && mouseX < x + NODE && mouseY >= y && mouseY < y + NODE;
  }
}
