package com.grahambartley.nightborn.screen;

public record ExperienceBar(int left, int top, int width) {

  public static final int MIN_WIDTH = 90;
  public static final int HEIGHT = 9;
  public static final int ROW_OFFSET = 10;
  static final int BORDER = 1;

  public static ExperienceBar under(final SkillPointsRow row, final int rowTop, final int width) {
    return new ExperienceBar(row.detailX(), rowTop + ROW_OFFSET, width);
  }

  public static int shown(final float experience) {
    return (int) Math.floor(Math.max(0.0F, experience));
  }

  public int filled(final float fraction) {
    return Math.round((width - BORDER * 2) * Math.max(0.0F, Math.min(1.0F, fraction)));
  }

  public int right() {
    return left + width;
  }

  public int bottom() {
    return top + HEIGHT;
  }

  public int innerLeft() {
    return left + BORDER;
  }

  public int innerTop() {
    return top + BORDER;
  }

  public int innerRight() {
    return right() - BORDER;
  }

  public int innerBottom() {
    return bottom() - BORDER;
  }

  public int centre() {
    return left + width / 2;
  }

  public boolean contains(final double mouseX, final double mouseY) {
    return mouseX >= left && mouseX < right() && mouseY >= top && mouseY < bottom();
  }
}
