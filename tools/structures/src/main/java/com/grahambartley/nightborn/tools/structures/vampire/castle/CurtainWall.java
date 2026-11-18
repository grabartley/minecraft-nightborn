package com.grahambartley.nightborn.tools.structures.vampire.castle;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Stone;

public final class CurtainWall {

  public static final int THICK = 2;
  public static final int RUIN_FROM = 13;
  public static final int RUIN_TO = 27;
  public static final int STAIR_X = 14;
  public static final int STAIR_Z = THICK;

  private CurtainWall() {}

  public static boolean onWall(final int x, final int z) {
    final int far = Castle.SIZE - 1;
    return x < THICK || z < THICK || x > far - THICK || z > far - THICK;
  }

  public static boolean outerFace(final int x, final int z) {
    final int far = Castle.SIZE - 1;
    return x == 0 || z == 0 || x == far || z == far;
  }

  public static boolean onWalk(final int x, final int z) {
    final int far = Castle.SIZE - 1;
    return onWall(x, z) && (x == THICK - 1 || z == THICK - 1 || x == far - 1 || z == far - 1);
  }

  public static int top(final Castle.Layout layout, final int x, final int z) {
    if (layout.ruinedWall() && x > Castle.SIZE - 1 - THICK && z >= RUIN_FROM && z <= RUIN_TO) {
      return Castle.WALL_TOP - 1 - Stone.roll(x, 0, z) % 6;
    }
    return Castle.WALL_TOP;
  }

  public static void build(final Template t, final Castle.Layout layout) {
    for (int x = 0; x < Castle.SIZE; x++) {
      for (int z = 0; z < Castle.SIZE; z++) {
        if (!onWall(x, z)) {
          continue;
        }
        final int top = top(layout, x, z);
        for (int y = 0; y <= top; y++) {
          t.set(x, y, z, y < Castle.FLOOR ? Castle.PLINTH.at(x, y, z) : Castle.WALL.at(x, y, z));
        }
        if (top == Castle.WALL_TOP && outerFace(x, z) && (x + z) % 2 == 0) {
          t.set(x, top + 1, z, Castle.WALL.at(x, top + 1, z));
        } else {
          t.set(x, top + 1, z, BlockState.AIR);
        }
        t.set(x, top + 2, z, BlockState.AIR);
      }
    }
    if (layout.ruinedWall()) {
      rubble(t);
    }
    stair(t);
  }

  private static void rubble(final Template t) {
    final int x = Castle.SIZE - 1 - THICK;
    for (int z = RUIN_FROM; z <= RUIN_TO; z++) {
      final int roll = Stone.roll(x, 1, z);
      if (roll < 45) {
        t.set(
            x,
            Castle.FLOOR,
            z,
            roll < 20 ? "cobblestone" : "nightborn:cracked_gothic_stone_bricks");
      } else if (roll < 60) {
        t.set(
            x,
            Castle.FLOOR,
            z,
            BlockState.of(
                "nightborn:gothic_stone_brick_slab", "type", "bottom", "waterlogged", false));
      }
    }
  }

  private static void stair(final Template t) {
    for (int i = 0; i < Castle.WALL_TOP - Castle.GROUND; i++) {
      final int x = STAIR_X + i;
      final int y = Castle.FLOOR + i;
      for (int below = Castle.FLOOR; below < y; below++) {
        t.set(x, below, STAIR_Z, Castle.WALL.at(x, below, STAIR_Z));
      }
      t.set(
          x,
          y,
          STAIR_Z,
          BlockState.of(
              Castle.STEP,
              "facing",
              "east",
              "half",
              "bottom",
              "shape",
              "straight",
              "waterlogged",
              false));
    }
  }

  public static void walkwayDoors(final Template t, final Tower tower) {
    for (int x = tower.cx() - tower.radius(); x <= tower.cx() + tower.radius(); x++) {
      for (int z = tower.cz() - tower.radius(); z <= tower.cz() + tower.radius(); z++) {
        if (tower.wall(x, z) && onWalk(x, z) && t.inside(x, 0, z)) {
          t.set(x, Castle.WALL_TOP + 1, z, BlockState.AIR);
          t.set(x, Castle.WALL_TOP + 2, z, BlockState.AIR);
        }
      }
    }
  }
}
