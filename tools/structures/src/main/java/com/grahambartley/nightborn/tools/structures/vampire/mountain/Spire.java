package com.grahambartley.nightborn.tools.structures.vampire.mountain;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;

public final class Spire {

  public static final String FINIAL = "lightning_rod";

  private Spire() {}

  public static boolean within(final int dx, final int dz, final int radius, final boolean round) {
    if (radius < 0) {
      return false;
    }
    return round
        ? dx * dx + dz * dz <= radius * radius + radius
        : Math.max(Math.abs(dx), Math.abs(dz)) <= radius;
  }

  public static boolean onRing(final int dx, final int dz, final int radius, final boolean round) {
    return within(dx, dz, radius, round) && !within(dx, dz, radius - 1, round);
  }

  public static String tile(final int dx, final int dz, final int radius, final boolean round) {
    final String stairs = MountainCastle.TILES.stairs();
    if (!round && Math.abs(dx) == radius && Math.abs(dz) == radius) {
      final String facing = dz > 0 ? "north" : "south";
      final String shape = dx < 0 == dz > 0 ? "outer_right" : "outer_left";
      return stair(stairs, facing, shape);
    }
    final boolean alongX = round ? Math.abs(dx) >= Math.abs(dz) : Math.abs(dx) == radius;
    if (alongX) {
      return stair(stairs, dx > 0 ? "west" : "east", "straight");
    }
    return stair(stairs, dz > 0 ? "north" : "south", "straight");
  }

  public static boolean sloped(final int radius, final int layer, final int layers) {
    return layers == 1 ? radius % 2 == 0 : layer == 0;
  }

  public static int height(final int radius, final int layers) {
    return layers * radius + 2;
  }

  public static int raise(
      final Template t,
      final int cx,
      final int cz,
      final int radius,
      final int from,
      final boolean round,
      final int layers) {
    int y = from;
    for (int r = radius; r >= 1; r--) {
      for (int layer = 0; layer < layers; layer++, y++) {
        final boolean sloped = sloped(r, layer, layers);
        for (int dx = -r; dx <= r; dx++) {
          for (int dz = -r; dz <= r; dz++) {
            if (onRing(dx, dz, r, round) && t.inside(cx + dx, y, cz + dz)) {
              t.set(
                  cx + dx,
                  y,
                  cz + dz,
                  sloped ? tile(dx, dz, r, round) : MountainCastle.TILES.full());
            }
          }
        }
      }
    }
    t.set(cx, y, cz, MountainCastle.TILES.full());
    t.set(
        cx,
        y + 1,
        cz,
        BlockState.of(FINIAL, "facing", "up", "powered", false, "waterlogged", false));
    return y + 1;
  }

  private static String stair(final String block, final String facing, final String shape) {
    return BlockState.of(
        block, "facing", facing, "half", "bottom", "shape", shape, "waterlogged", false);
  }
}
