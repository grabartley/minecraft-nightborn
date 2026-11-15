package com.grahambartley.nightborn.client;

public final class HudSigns {

  static final int X = 4;
  static final int TOP = 4;
  static final int ICON = 18;
  static final int GAP = 2;
  static final int TEXT_OFFSET_X = ICON + 4;
  static final int TEXT_OFFSET_Y = 5;

  private HudSigns() {}

  static int y(final int index) {
    return TOP + Math.max(0, index) * (ICON + GAP);
  }
}
