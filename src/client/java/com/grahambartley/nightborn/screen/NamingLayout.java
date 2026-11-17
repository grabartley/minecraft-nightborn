package com.grahambartley.nightborn.screen;

public record NamingLayout(int left, int top, int width, int height) {

  public static final int WIDTH = 220;
  public static final int HEIGHT = 110;
  static final int PAD = 10;
  static final int FIELD_TOP = 34;
  static final int FIELD_HEIGHT = 20;
  static final int PROBLEM_TOP = 60;
  static final int BUTTON_TOP = 80;
  static final int BUTTON_WIDTH = 96;
  static final int BUTTON_HEIGHT = 20;

  public static NamingLayout of(final int screenWidth, final int screenHeight) {
    return new NamingLayout(
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

  public int buttonLeft() {
    return centre() - BUTTON_WIDTH / 2;
  }

  public int buttonTop() {
    return top + BUTTON_TOP;
  }

  public int titleTop() {
    return top + PAD;
  }
}
