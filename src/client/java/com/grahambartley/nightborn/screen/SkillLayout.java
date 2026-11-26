package com.grahambartley.nightborn.screen;

import com.grahambartley.nightborn.skill.Skill;

public record SkillLayout(
    int left, int top, int width, int height, int branchWidth, int rowStep, double zoom) {

  public static final int NODE = 22;
  static final int ROW_STEP = 28;
  static final int MIN_ROW_STEP = NODE + 1;
  static final int HEADER = 56;
  static final int BAR_GAP = 6;
  static final int POINTS_TOP = 20;
  static final int BANNER = 13;
  static final int BANNER_PAD = 8;
  static final int BANNER_GAP = 3;
  static final int BANNER_TEXT_INSET = 3;
  static final int MIN_WIDTH = 200;
  static final int PAD = 8;
  static final int MAX_BRANCH_WIDTH = 96;
  static final int MAX_INSET = 6;
  static final int NODE_GAP = 2;
  static final int MIN_BRANCH_WIDTH = NODE * (Skill.MAX_COLUMN + 1);
  static final double ROUNDING = 1.0E-6;

  public static SkillLayout of(
      final int screenWidth,
      final int screenHeight,
      final int branches,
      final int rows,
      final int headerWidth,
      final int footer) {
    final int count = Math.max(1, branches);
    final int shownRows = Math.max(1, Math.min(rows, Skill.MAX_ROW + 1));
    final int fixed = HEADER + NODE + BAR_GAP + PowerBarRow.SLOT + PAD * 2;
    final int leastWidth =
        Math.max(MIN_BRANCH_WIDTH * count + PAD * 2, Math.max(MIN_WIDTH, headerWidth + PAD * 2));
    final int leastHeight = fixed + MIN_ROW_STEP * (shownRows - 1) + footer;
    final double zoom =
        Math.min(
            1.0, Math.min((double) screenWidth / leastWidth, (double) screenHeight / leastHeight));
    final int roomWidth = (int) Math.floor(screenWidth / zoom + ROUNDING);
    final int roomHeight = (int) Math.floor(screenHeight / zoom + ROUNDING);
    final int branchWidth =
        Math.max(MIN_BRANCH_WIDTH, Math.min(MAX_BRANCH_WIDTH, (roomWidth - PAD * 4) / count));
    final int width =
        Math.max(branchWidth * count + PAD * 2, Math.max(MIN_WIDTH, headerWidth + PAD * 2));
    final int rowStep =
        shownRows == 1
            ? ROW_STEP
            : Math.max(
                MIN_ROW_STEP, Math.min(ROW_STEP, (roomHeight - footer - fixed) / (shownRows - 1)));
    final int height = fixed + rowStep * (shownRows - 1);
    return new SkillLayout(
        Math.max(0, (roomWidth - width) / 2),
        Math.max(0, (roomHeight - height - footer) / 2),
        width,
        height,
        branchWidth,
        rowStep,
        zoom);
  }

  public double unzoom(final double screen) {
    return screen / zoom;
  }

  public int bottom() {
    return top + height;
  }

  public int branchLeft(final int index, final int branches) {
    final int used = branchWidth * Math.max(1, branches);
    return left + (width - used) / 2 + index * branchWidth;
  }

  public int nodeX(final int branchIndex, final int branches, final int column) {
    final int inset =
        Math.max(
            0,
            Math.min(
                MAX_INSET, (branchWidth - MIN_BRANCH_WIDTH - NODE_GAP * Skill.MAX_COLUMN) / 2));
    final int step = (branchWidth - NODE - inset * 2) / Skill.MAX_COLUMN;
    final int span = step * Skill.MAX_COLUMN + NODE;
    return branchLeft(branchIndex, branches) + (branchWidth - span) / 2 + column * step;
  }

  public int nodeY(final int row) {
    return top + HEADER + row * rowStep;
  }

  public int powerBarY() {
    return top + height - PAD - PowerBarRow.SLOT;
  }

  public int pointsY() {
    return top + POINTS_TOP;
  }

  public int bannerY() {
    return top + HEADER - BANNER - BANNER_GAP;
  }

  public int bannerTextY() {
    return bannerY() + BANNER_TEXT_INSET;
  }

  public int bannerWidth(final int textWidth) {
    return Math.min(branchWidth - 2, textWidth + BANNER_PAD * 2);
  }

  public static boolean contains(
      final int x, final int y, final double mouseX, final double mouseY) {
    return mouseX >= x && mouseX < x + NODE && mouseY >= y && mouseY < y + NODE;
  }
}
