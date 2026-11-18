package com.grahambartley.nightborn.screen;

public record HouseNamingLayout(int left, int top, int width, int height) {

  public static final int WIDTH = 260;
  public static final int HEIGHT = 196;
  static final int PAD = 10;
  static final int FIELD_TOP = 34;
  static final int FIELD_HEIGHT = 20;
  static final int PROBLEM_TOP = 60;
  static final int ROWS_TOP = 78;
  static final int ROW_GAP = 26;
  static final int ROWS = 3;
  static final int STEP_WIDTH = 20;
  static final int ROW_HEIGHT = 20;
  static final int PREVIEW_SIZE = 72;
  static final int BUTTON_WIDTH = 96;
  static final int BUTTON_HEIGHT = 20;

  public static HouseNamingLayout of(final int screenWidth, final int screenHeight) {
    return new HouseNamingLayout(
        Math.max(0, (screenWidth - WIDTH) / 2),
        Math.max(0, (screenHeight - HEIGHT) / 2),
        WIDTH,
        HEIGHT);
  }

  public int centre() {
    return left + width / 2;
  }

  public int fieldLeft() {
    return left + PAD;
  }

  public int fieldWidth() {
    return width - PAD * 2;
  }

  public int fieldTop() {
    return top + FIELD_TOP;
  }

  public int problemTop() {
    return top + PROBLEM_TOP;
  }

  public int titleTop() {
    return top + PAD;
  }

  public int previewLeft() {
    return left + PAD;
  }

  public int previewTop() {
    return top + ROWS_TOP;
  }

  public int rowTop(final int row) {
    return top + ROWS_TOP + Math.clamp(row, 0, ROWS - 1) * ROW_GAP;
  }

  public int stepBackLeft() {
    return previewLeft() + PREVIEW_SIZE + PAD;
  }

  public int stepOnLeft() {
    return left + width - PAD - STEP_WIDTH;
  }

  public int labelCentre() {
    return (stepBackLeft() + STEP_WIDTH + stepOnLeft()) / 2;
  }

  public int buttonLeft() {
    return centre() - BUTTON_WIDTH / 2;
  }

  public int buttonTop() {
    return top + height - PAD - BUTTON_HEIGHT;
  }
}
