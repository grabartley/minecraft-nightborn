package com.grahambartley.nightborn.screen;

public record NamingLayout(int left, int top, int width, int height) implements NameEntryBox {

  public static final int WIDTH = 220;
  public static final int HEIGHT = 110;
  static final int BUTTON_TOP = 80;

  public static NamingLayout of(final int screenWidth, final int screenHeight) {
    return new NamingLayout(
        NameEntryBox.centred(screenWidth, WIDTH),
        NameEntryBox.centred(screenHeight, HEIGHT),
        WIDTH,
        HEIGHT);
  }

  @Override
  public int buttonTop() {
    return top + BUTTON_TOP;
  }
}
