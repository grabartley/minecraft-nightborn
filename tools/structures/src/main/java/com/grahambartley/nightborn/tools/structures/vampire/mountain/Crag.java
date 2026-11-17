package com.grahambartley.nightborn.tools.structures.vampire.mountain;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Stone;

public final class Crag {

  public static final int UPPER_WEST = 6;
  public static final int UPPER_EAST = 56;
  public static final int UPPER_NORTH = 4;
  public static final int UPPER_SOUTH = 44;
  public static final int LOWER_WEST = 10;
  public static final int LOWER_EAST = 52;
  public static final int LOWER_SOUTH = 58;
  public static final int HEADROOM = 10;
  public static final int SKIRT = 3;
  public static final int NONE = -1;

  private Crag() {}

  public static int level(final int x, final int z) {
    if (x >= UPPER_WEST && x <= UPPER_EAST && z >= UPPER_NORTH && z <= UPPER_SOUTH) {
      return MountainCastle.UP;
    }
    if (x >= LOWER_WEST && x <= LOWER_EAST && z > UPPER_SOUTH && z <= LOWER_SOUTH) {
      return MountainCastle.LOW;
    }
    return NONE;
  }

  public static String rock(final int x, final int y, final int z) {
    final int roll = Stone.roll(x, y, z);
    if (roll < 56) {
      return "stone";
    }
    if (roll < 80) {
      return "andesite";
    }
    if (roll < 93) {
      return "cobblestone";
    }
    return "mossy_cobblestone";
  }

  public static String yard(final int x, final int z) {
    final int roll = Stone.roll(x, 3, z);
    if (roll < 46) {
      return "cobblestone";
    }
    if (roll < 70) {
      return "stone";
    }
    if (roll < 88) {
      return "andesite";
    }
    return "mossy_cobblestone";
  }

  public static void build(final Template t) {
    for (int x = 0; x < MountainCastle.SIZE; x++) {
      for (int z = 0; z < MountainCastle.SIZE; z++) {
        final int level = level(x, z);
        if (level == NONE) {
          continue;
        }
        for (int y = 0; y < level; y++) {
          t.set(x, y, z, rock(x, y, z));
        }
        t.set(x, level, z, yard(x, z));
        for (int y = level + 1; y <= MountainCastle.UP + HEADROOM; y++) {
          t.set(x, y, z, BlockState.AIR);
        }
      }
    }
  }

  public static void column(final Template t, final int x, final int z, final int level) {
    for (int y = 0; y <= level; y++) {
      t.set(x, y, z, y < level - footing(x, z) ? rock(x, y, z) : MountainCastle.FOOT.at(x, y, z));
    }
  }

  public static int footing(final int x, final int z) {
    return 3 + Stone.roll(x, 11, z) % 4;
  }

  public static int skirtTop(final int level, final int distance, final int x, final int z) {
    return level - 2 - 5 * distance - Stone.roll(x, 7, z) % 5;
  }

  public static void skirt(final Template t) {
    final int size = MountainCastle.SIZE;
    final int[][] tops = new int[size][size];
    for (int x = 0; x < size; x++) {
      for (int z = 0; z < size; z++) {
        tops[x][z] = t.get(x, 0, z) == null ? skirtFor(t, x, z) : NONE;
      }
    }
    for (int x = 0; x < size; x++) {
      for (int z = 0; z < size; z++) {
        for (int y = 0; y <= tops[x][z]; y++) {
          t.set(x, y, z, rock(x, y, z));
        }
      }
    }
  }

  private static int skirtFor(final Template t, final int x, final int z) {
    for (int distance = 1; distance <= SKIRT; distance++) {
      int best = NONE;
      for (int dx = -distance; dx <= distance; dx++) {
        for (int dz = -distance; dz <= distance; dz++) {
          final int nx = x + dx;
          final int nz = z + dz;
          if (t.inside(nx, 0, nz) && t.get(nx, 0, nz) != null) {
            best = Math.max(best, nz > UPPER_SOUTH ? MountainCastle.LOW : MountainCastle.UP);
          }
        }
      }
      if (best != NONE) {
        return skirtTop(best, distance, x, z);
      }
    }
    return NONE;
  }
}
