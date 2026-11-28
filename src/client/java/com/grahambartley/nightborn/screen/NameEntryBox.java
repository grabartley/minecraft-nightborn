package com.grahambartley.nightborn.screen;

public interface NameEntryBox {

  int PAD = 10;
  int FIELD_TOP = 34;
  int FIELD_HEIGHT = 20;
  int PROBLEM_TOP = 60;
  int BUTTON_WIDTH = 96;
  int BUTTON_HEIGHT = 20;

  int left();

  int top();

  int width();

  int height();

  int buttonTop();

  static int centred(final int screen, final int size) {
    return Math.max(0, (screen - size) / 2);
  }

  default int centre() {
    return left() + width() / 2;
  }

  default int titleTop() {
    return top() + PAD;
  }

  default int fieldLeft() {
    return left() + PAD;
  }

  default int fieldWidth() {
    return width() - PAD * 2;
  }

  default int fieldTop() {
    return top() + FIELD_TOP;
  }

  default int problemTop() {
    return top() + PROBLEM_TOP;
  }

  default int buttonLeft() {
    return centre() - BUTTON_WIDTH / 2;
  }
}
