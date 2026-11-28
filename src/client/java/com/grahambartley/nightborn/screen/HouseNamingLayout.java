package com.grahambartley.nightborn.screen;

public record HouseNamingLayout(int left, int top, int width, int height) implements NameEntryBox {

  public static final int WIDTH = 260;
  public static final int HEIGHT = 196;
  static final int ROWS_TOP = 78;
  static final int ROW_GAP = 26;
  static final int ROWS = 3;
  static final int STEP_WIDTH = 20;
  static final int ROW_HEIGHT = 20;
  static final int PREVIEW_SIZE = 72;

  public static HouseNamingLayout of(final int screenWidth, final int screenHeight) {
    return new HouseNamingLayout(
        NameEntryBox.centred(screenWidth, WIDTH),
        NameEntryBox.centred(screenHeight, HEIGHT),
        WIDTH,
        HEIGHT);
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

  @Override
  public int buttonTop() {
    return top + height - PAD - BUTTON_HEIGHT;
  }
}
