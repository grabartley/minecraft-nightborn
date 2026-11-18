package com.grahambartley.nightborn.tools.structures.vampire;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;

public final class Roof {

  public record Cover(String stairs, String full, String slab) {}

  private Roof() {}

  public static int gable(
      final Template t,
      final Axis ridge,
      final int from,
      final int to,
      final int lowWall,
      final int highWall,
      final int eave,
      final Cover cover,
      final Stone gable) {
    return gable(t, ridge, from, to, lowWall, highWall, eave, cover, gable, true);
  }

  public static int gable(
      final Template t,
      final Axis ridge,
      final int from,
      final int to,
      final int lowWall,
      final int highWall,
      final int eave,
      final Cover cover,
      final Stone gable,
      final boolean overhang) {
    return gable(t, ridge, from, to, lowWall, highWall, eave, cover, gable, overhang, false);
  }

  public static int gable(
      final Template t,
      final Axis ridge,
      final int from,
      final int to,
      final int lowWall,
      final int highWall,
      final int eave,
      final Cover cover,
      final Stone gable,
      final boolean overhang,
      final boolean ledged) {
    final Axis across = ridge.across();
    int k = overhang ? 0 : 1;
    while (true) {
      final int low = lowWall - 1 + k;
      final int high = highWall + 1 - k;
      final int y = eave + k;
      if (low > high) {
        return y - 1;
      }
      for (int a = Math.min(from, to); a <= Math.max(from, to); a++) {
        if (low == high) {
          t.set(ridge.x(a, low), y, ridge.z(a, low), cover.full());
          t.set(
              ridge.x(a, low),
              y + 1,
              ridge.z(a, low),
              BlockState.of(cover.slab(), "type", "bottom"));
        } else if (ledged && k % 2 == 1) {
          t.set(ridge.x(a, low), y, ridge.z(a, low), cover.full());
          t.set(ridge.x(a, high), y, ridge.z(a, high), cover.full());
        } else {
          stair(t, ridge, a, low, y, across.high(), cover);
          stair(t, ridge, a, high, y, across.low(), cover);
        }
      }
      if (low == high) {
        return y + 1;
      }
      if (k == 0) {
        sealEave(t, ridge, from, to, lowWall, highWall, y, gable);
      }
      if (k > 0) {
        for (int c = low + 1; c < high; c++) {
          end(t, ridge, from, c, y, gable);
          end(t, ridge, to, c, y, gable);
        }
      }
      k++;
    }
  }

  public static void fillAttic(
      final Template t,
      final int x0,
      final int z0,
      final int x1,
      final int z1,
      final int fromY,
      final String block) {
    for (int x = x0; x <= x1; x++) {
      for (int z = z0; z <= z1; z++) {
        for (int y = fromY; y < t.sizeY() && BlockState.AIR.equals(t.get(x, y, z)); y++) {
          t.set(x, y, z, block);
        }
      }
    }
  }

  private static void sealEave(
      final Template t,
      final Axis ridge,
      final int from,
      final int to,
      final int lowWall,
      final int highWall,
      final int y,
      final Stone gable) {
    for (int a = Math.min(from, to); a <= Math.max(from, to); a++) {
      for (int c = lowWall; c <= highWall; c++) {
        final boolean wallLine = a == from || a == to || c == lowWall || c == highWall;
        final String there = t.get(ridge.x(a, c), y, ridge.z(a, c));
        if (wallLine && (there == null || BlockState.AIR.equals(there))) {
          end(t, ridge, a, c, y, gable);
        }
      }
    }
  }

  private static void stair(
      final Template t,
      final Axis ridge,
      final int along,
      final int across,
      final int y,
      final String facing,
      final Cover cover) {
    t.set(
        ridge.x(along, across),
        y,
        ridge.z(along, across),
        BlockState.of(cover.stairs(), "facing", facing, "half", "bottom", "shape", "straight"));
  }

  private static void end(
      final Template t,
      final Axis ridge,
      final int along,
      final int across,
      final int y,
      final Stone gable) {
    final int x = ridge.x(along, across);
    final int z = ridge.z(along, across);
    t.set(x, y, z, gable.at(x, y, z));
  }
}
