package com.grahambartley.nightborn.tools.structures.vampire;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;

public final class Openings {

  private Openings() {}

  public static void arch(
      final Template t,
      final Axis wall,
      final int from,
      final int to,
      final int line,
      final int floor,
      final int height,
      final String stairs) {
    final int top = floor + height - 1;
    for (int a = from; a <= to; a++) {
      for (int y = floor; y <= top; y++) {
        t.set(wall.x(a, line), y, wall.z(a, line), BlockState.AIR);
      }
    }
    if (to - from >= 2) {
      shoulder(t, wall, from, line, top, wall.low(), stairs);
      shoulder(t, wall, to, line, top, wall.high(), stairs);
    }
  }

  public static void lancet(
      final Template t,
      final Axis wall,
      final int along,
      final int line,
      final int sill,
      final int height,
      final String glass) {
    for (int y = sill; y < sill + height; y++) {
      t.set(wall.x(along, line), y, wall.z(along, line), glass);
    }
  }

  private static void shoulder(
      final Template t,
      final Axis wall,
      final int along,
      final int line,
      final int y,
      final String facing,
      final String stairs) {
    t.set(
        wall.x(along, line),
        y,
        wall.z(along, line),
        BlockState.of(stairs, "facing", facing, "half", "top", "shape", "straight"));
  }
}
