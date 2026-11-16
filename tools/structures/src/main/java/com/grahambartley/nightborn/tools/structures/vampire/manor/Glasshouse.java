package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.F0;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.WING_W;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Stone;

public final class Glasshouse {

  public static final int WEST = 5;
  public static final int EAST = WING_W - 1;
  public static final int NORTH = 9;
  public static final int SOUTH = 17;
  public static final int DOOR_Z = 13;
  public static final int EAVE = F0 + 5;
  public static final int TREE_X = WEST + 4;
  public static final int TREE_Z = 13;
  static final Stone TILES =
      new Stone("stone_bricks", "mossy_stone_bricks", "cracked_stone_bricks", 60);

  private Glasshouse() {}

  public static int roofY(final int x) {
    return EAVE + Math.min(x - WEST, EAST - x);
  }

  public static void build(final Template t, final boolean fallen) {
    final int broken = fallen ? 65 : 15;
    for (int x = WEST; x <= EAST; x++) {
      for (int z = NORTH; z <= SOUTH; z++) {
        for (int y = 0; y < F0; y++) {
          t.set(x, y, z, Manor.DRESSING.at(x, y, z));
        }
        t.set(x, F0, z, TILES.at(x, F0, z));
        final boolean wall = x == WEST || z == NORTH || z == SOUTH;
        for (int y = F0 + 1; y < roofY(x); y++) {
          if (!wall) {
            t.set(x, y, z, BlockState.AIR);
          } else if (y == F0 + 1) {
            t.set(x, y, z, Manor.BRICK.at(x, y, z));
          } else {
            t.set(x, y, z, glazing(x, y, z, broken));
          }
        }
        t.set(x, roofY(x), z, Stone.roll(x, roofY(x), z) < broken ? BlockState.AIR : "glass");
      }
    }
    entrance(t);
    beds(t);
    tree(t, fallen);
    bench(t);
  }

  static String glazing(final int x, final int y, final int z, final int broken) {
    final boolean post = (x + z) % 3 == 0;
    if (post) {
      return "iron_bars";
    }
    return Stone.roll(x, y, z) < broken ? BlockState.AIR : Manor.PANE;
  }

  private static void entrance(final Template t) {
    Window.door(t, WEST, F0 + 1, DOOR_Z, "west");
    for (int y = F0 + 3; y < roofY(WEST); y++) {
      t.set(WEST, y, DOOR_Z, "iron_bars");
    }
    Parts.flight(t, Manor.TRIM, ManorFront.STONE_FILL, WEST - 1, DOOR_Z, "east", F0, F0, 0);
    for (int y = F0 + 1; y <= F0 + 2; y++) {
      t.set(EAST, y, DOOR_Z, BlockState.AIR);
      t.set(WEST + 1, y, DOOR_Z, BlockState.AIR);
    }
  }

  private static void beds(final Template t) {
    for (int z = NORTH + 1; z < SOUTH; z++) {
      if (z == DOOR_Z) {
        continue;
      }
      t.set(WEST + 1, F0, z, "podzol");
      final int roll = Stone.roll(WEST + 1, F0 + 1, z);
      if (roll < 35) {
        t.set(WEST + 1, F0 + 1, z, BlockState.of("large_fern", "half", "lower"));
        t.set(WEST + 1, F0 + 2, z, BlockState.of("large_fern", "half", "upper"));
      } else if (roll < 65) {
        t.set(WEST + 1, F0 + 1, z, "flowering_azalea");
      } else {
        t.set(WEST + 1, F0 + 1, z, roll < 85 ? "fern" : "dead_bush");
      }
    }
    for (int x = WEST + 2; x <= EAST; x++) {
      if (Stone.roll(x, F0 + 1, NORTH + 1) < 50) {
        t.set(x, F0 + 1, NORTH + 1, "moss_carpet");
      }
    }
  }

  private static void tree(final Template t, final boolean fallen) {
    for (int dx = -1; dx <= 1; dx++) {
      for (int dz = -1; dz <= 1; dz++) {
        t.set(TREE_X + dx, F0 + 1, TREE_Z + dz, Manor.BRICK.at(TREE_X + dx, F0 + 1, TREE_Z + dz));
      }
    }
    t.set(TREE_X, F0 + 1, TREE_Z, "rooted_dirt");
    final int crown = fallen ? roofY(TREE_X) + 2 : roofY(TREE_X) - 3;
    for (int y = F0 + 2; y <= crown; y++) {
      t.set(TREE_X, y, TREE_Z, Parts.log("dark_oak_log", "y"));
    }
    for (int dx = -2; dx <= 2; dx++) {
      for (int dz = -2; dz <= 2; dz++) {
        for (int dy = 0; dy <= 1; dy++) {
          final boolean reach = Math.abs(dx) + Math.abs(dz) <= 3 - dy;
          final int x = TREE_X + dx;
          final int z = TREE_Z + dz;
          final int y = crown + dy;
          final boolean underRoof = fallen || y < roofY(x);
          if (reach && underRoof && (dx != 0 || dz != 0 || dy == 1) && Stone.roll(x, y, z) < 80) {
            t.set(x, y, z, Parts.leaves("azalea_leaves"));
          }
        }
      }
    }
    t.set(TREE_X - 1, crown - 1, TREE_Z, Parts.log("dark_oak_log", "x"));
    t.set(TREE_X, crown - 2, TREE_Z + 1, Parts.log("dark_oak_log", "z"));
  }

  private static void bench(final Template t) {
    for (int x = WEST + 3; x <= WEST + 5; x++) {
      t.set(x, F0 + 1, SOUTH - 1, Parts.slab("dark_oak_slab", "top"));
    }
    t.set(WEST + 3, F0 + 2, SOUTH - 1, "potted_wither_rose");
    t.set(WEST + 4, F0 + 2, SOUTH - 1, "potted_dead_bush");
    t.set(WEST + 5, F0 + 2, SOUTH - 1, "flower_pot");
    t.set(EAST - 1, F0 + 1, NORTH + 1, Parts.stair(Manor.TRIM, "west"));
    t.set(EAST - 1, F0 + 1, NORTH + 2, Parts.stair(Manor.TRIM, "west"));
    t.set(WEST + 1, roofY(WEST + 1) - 1, SOUTH - 1, "cobweb");
    t.set(EAST - 1, roofY(EAST - 1) - 1, NORTH + 1, Parts.vine("up"));
    t.set(WEST + 2, roofY(WEST + 2) - 1, TREE_Z + 2, Parts.vine("up"));
  }
}
