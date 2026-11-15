package com.grahambartley.nightborn.client;

import java.util.ArrayList;
import java.util.List;

public final class PowerBarLayout {

  public static final int SLOT = 22;
  static final int HOTBAR_HALF_WIDTH = 91;
  static final int OFFHAND_WIDTH = 29;
  static final int GAP = 6;
  static final int EDGE = 2;

  public record Point(int x, int y) {}

  private PowerBarLayout() {}

  public static List<Point> slots(
      final int screenWidth,
      final int screenHeight,
      final int count,
      final boolean offhandOnRight) {
    final List<Point> points = new ArrayList<>();
    final int start =
        screenWidth / 2 + HOTBAR_HALF_WIDTH + GAP + (offhandOnRight ? OFFHAND_WIDTH : 0);
    final int y = screenHeight - SLOT;
    if (start + count * SLOT <= screenWidth - EDGE) {
      for (int i = 0; i < count; i++) {
        points.add(new Point(start + i * SLOT, y));
      }
      return points;
    }
    final int x = screenWidth - SLOT - EDGE;
    for (int i = 0; i < count; i++) {
      points.add(new Point(x, y - (count - 1 - i) * SLOT - SLOT - EDGE));
    }
    return points;
  }
}
