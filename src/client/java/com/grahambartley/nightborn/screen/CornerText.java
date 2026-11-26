package com.grahambartley.nightborn.screen;

public final class CornerText {

  public static final float SCALE = 0.5F;
  static final int MARGIN = 1;
  static final int GLYPH_HEIGHT = 8;

  private CornerText() {}

  public static int rightX(final int box, final int size, final int inset, final int textWidth) {
    return box + size - inset - MARGIN - scaled(textWidth);
  }

  public static int bottomY(final int box, final int size, final int inset) {
    return box + size - inset - MARGIN - scaled(GLYPH_HEIGHT);
  }

  public static int leftX(final int box, final int inset) {
    return box + inset + MARGIN;
  }

  public static int topY(final int box, final int inset) {
    return box + inset + MARGIN;
  }

  static int scaled(final int length) {
    return (int) Math.ceil(length * SCALE);
  }
}
