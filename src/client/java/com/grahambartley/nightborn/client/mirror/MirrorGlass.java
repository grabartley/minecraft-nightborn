package com.grahambartley.nightborn.client.mirror;

public record MirrorGlass(int left, int top, int right, int bottom) {

  public static final int WIDTH = 74;
  public static final int HEIGHT = 96;
  public static final int FRAME = 5;
  public static final int FIGURE_SIZE = 34;

  public static MirrorGlass centred(final int screenWidth, final int screenHeight) {
    final int left = (screenWidth - WIDTH) / 2;
    final int top = (screenHeight - HEIGHT) / 2;
    return new MirrorGlass(left, top, left + WIDTH, top + HEIGHT);
  }

  public int glassLeft() {
    return left + FRAME;
  }

  public int glassTop() {
    return top + FRAME;
  }

  public int glassRight() {
    return right - FRAME;
  }

  public int glassBottom() {
    return bottom - FRAME;
  }
}
