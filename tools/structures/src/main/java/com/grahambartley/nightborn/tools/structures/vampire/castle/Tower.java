package com.grahambartley.nightborn.tools.structures.vampire.castle;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Stone;

public record Tower(int cx, int cz, int radius, int height, boolean round) {

  public static final String FINIAL = "lightning_rod";

  public boolean inside(final int x, final int z, final int r) {
    final int dx = x - cx;
    final int dz = z - cz;
    if (r < 0) {
      return false;
    }
    return round ? dx * dx + dz * dz <= r * r + r : Math.max(Math.abs(dx), Math.abs(dz)) <= r;
  }

  public boolean wall(final int x, final int z) {
    return inside(x, z, radius) && !inside(x, z, radius - 1);
  }

  public int roofTop() {
    return height + 1 + 2 * (radius + 2);
  }

  public void build(final Template t, final Stone stone, final int... floors) {
    for (int x = cx - radius; x <= cx + radius; x++) {
      for (int z = cz - radius; z <= cz + radius; z++) {
        if (!inside(x, z, radius) || !t.inside(x, 0, z)) {
          continue;
        }
        for (int y = 0; y <= height; y++) {
          if (wall(x, z) || y < Castle.GROUND || y == height) {
            t.set(x, y, z, y < Castle.FLOOR ? Castle.PLINTH.at(x, y, z) : stone.at(x, y, z));
          } else if (y == Castle.GROUND || isFloor(y, floors)) {
            t.set(x, y, z, Castle.PLANKS);
          } else {
            t.set(x, y, z, BlockState.AIR);
          }
        }
      }
    }
    slits(t);
    roof(t);
  }

  private static boolean isFloor(final int y, final int[] floors) {
    for (final int floor : floors) {
      if (floor == y) {
        return true;
      }
    }
    return false;
  }

  private void slits(final Template t) {
    for (int y = Castle.FLOOR + 3; y < height - 2; y += 6) {
      for (final int[] side : new int[][] {{radius, 0}, {-radius, 0}, {0, radius}, {0, -radius}}) {
        final int x = cx + side[0];
        final int z = cz + side[1];
        if (t.inside(x, y, z)) {
          t.set(x, y, z, "iron_bars");
          t.set(x, y + 1, z, "iron_bars");
        }
      }
    }
  }

  private void roof(final Template t) {
    int y = height + 1;
    for (int r = radius + 1; r >= 0; r--) {
      for (int layer = 0; layer < 2; layer++, y++) {
        for (int x = cx - r; x <= cx + r; x++) {
          for (int z = cz - r; z <= cz + r; z++) {
            if (!t.inside(x, y, z) || !inside(x, z, r)) {
              continue;
            }
            if (!inside(x, z, r - 1)) {
              t.set(x, y, z, layer == 0 ? Castle.ROOF.full() : Castle.SHINGLE);
            } else {
              t.set(x, y, z, BlockState.AIR);
            }
          }
        }
      }
    }
    if (t.inside(cx, y, cz)) {
      t.set(
          cx, y, cz, BlockState.of(FINIAL, "facing", "up", "powered", false, "waterlogged", false));
    }
  }

  public void door(final Template t, final int x, final int z) {
    t.set(x, Castle.FLOOR, z, BlockState.AIR);
    t.set(x, Castle.FLOOR + 1, z, BlockState.AIR);
  }
}
