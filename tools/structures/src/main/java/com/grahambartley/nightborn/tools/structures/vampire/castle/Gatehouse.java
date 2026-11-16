package com.grahambartley.nightborn.tools.structures.vampire.castle;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Axis;
import com.grahambartley.nightborn.tools.structures.vampire.Openings;

public final class Gatehouse {

  public static final int WEST = Castle.MID - 5;
  public static final int EAST = Castle.MID + 5;
  public static final int NORTH = Castle.SIZE - 6;
  public static final int SOUTH = Castle.SIZE - 1;
  public static final int TOP = 14;
  public static final int GATE_HEIGHT = 5;
  public static final int UPPER_FLOOR = 8;

  private Gatehouse() {}

  public static void build(final Template t) {
    for (int x = WEST; x <= EAST; x++) {
      for (int z = NORTH; z <= SOUTH; z++) {
        final boolean shell = x == WEST || x == EAST || z == NORTH || z == SOUTH;
        for (int y = 0; y <= TOP; y++) {
          final String stone =
              y < Castle.FLOOR ? Castle.PLINTH.at(x, y, z) : Castle.WALL.at(x, y, z);
          if (shell || y < Castle.FLOOR + GATE_HEIGHT + 1 || y == UPPER_FLOOR || y == TOP) {
            t.set(x, y, z, stone);
          } else {
            t.set(x, y, z, BlockState.AIR);
          }
        }
        if ((shell && (x + z) % 2 == 0)) {
          t.set(x, TOP + 1, z, Castle.WALL.at(x, TOP + 1, z));
        }
      }
    }
    for (int z = NORTH; z <= SOUTH; z++) {
      Openings.arch(
          t, Axis.X, Castle.MID - 1, Castle.MID + 1, z, Castle.FLOOR, GATE_HEIGHT, Castle.STEP);
      t.set(Castle.MID - 1, Castle.GROUND, z, "polished_blackstone_bricks");
      t.set(Castle.MID, Castle.GROUND, z, "polished_blackstone_bricks");
      t.set(Castle.MID + 1, Castle.GROUND, z, "polished_blackstone_bricks");
    }
    for (int x = Castle.MID - 1; x <= Castle.MID + 1; x++) {
      t.set(x, Castle.FLOOR + GATE_HEIGHT - 2, SOUTH - 2, "iron_bars");
    }
    for (final int x : new int[] {WEST + 2, EAST - 2}) {
      t.set(x, UPPER_FLOOR + 3, SOUTH, "iron_bars");
      t.set(x, UPPER_FLOOR + 4, SOUTH, "iron_bars");
    }
    t.set(
        Castle.MID,
        Castle.FLOOR + GATE_HEIGHT - 1,
        NORTH,
        BlockState.of("soul_lantern", "hanging", true, "waterlogged", false));
    for (final int x : new int[] {WEST, EAST}) {
      new Tower(x, SOUTH, 2, TOP + 3, true).build(t, Castle.WALL);
    }
  }
}
