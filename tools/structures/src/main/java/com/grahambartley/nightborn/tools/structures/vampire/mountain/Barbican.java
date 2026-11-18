package com.grahambartley.nightborn.tools.structures.vampire.mountain;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Axis;
import com.grahambartley.nightborn.tools.structures.vampire.Furnishing;
import com.grahambartley.nightborn.tools.structures.vampire.Openings;
import com.grahambartley.nightborn.tools.structures.vampire.Roof;
import com.grahambartley.nightborn.tools.structures.vampire.manor.Parts;

public final class Barbican {

  public static final int WEST = MountainCastle.MID - 5;
  public static final int EAST = MountainCastle.MID + 5;
  public static final int NORTH = 54;
  public static final int SOUTH = 61;
  public static final int GATE_HEIGHT = 4;
  public static final int ROOM = Ramparts.LOW_TOP;
  public static final int TOP = MountainCastle.LOW + 14;

  private Barbican() {}

  public static void build(final Template t) {
    for (int x = WEST; x <= EAST; x++) {
      for (int z = NORTH; z <= SOUTH; z++) {
        final boolean shell = x == WEST || x == EAST || z == NORTH || z == SOUTH;
        Crag.column(t, x, z, MountainCastle.LOW);
        for (int y = MountainCastle.LOW + 1; y <= TOP; y++) {
          if (shell || y < ROOM) {
            t.set(x, y, z, MountainCastle.WALL.at(x, y, z));
          } else if (y == ROOM || y == TOP) {
            t.set(x, y, z, MountainCastle.PLANKS);
          } else {
            t.set(x, y, z, BlockState.AIR);
          }
        }
      }
    }
    passage(t);
    room(t);
    Roof.gable(
        t,
        Axis.X,
        WEST,
        EAST,
        NORTH,
        SOUTH,
        TOP + 1,
        MountainCastle.TILES,
        MountainCastle.PLASTER,
        true,
        true);
    Roof.fillAttic(t, WEST + 1, NORTH + 1, EAST - 1, SOUTH - 1, TOP + 1, MountainCastle.PLANKS);
  }

  private static void passage(final Template t) {
    final int floor = MountainCastle.LOW + 1;
    for (int z = NORTH; z <= SOUTH; z++) {
      Openings.arch(
          t,
          Axis.X,
          MountainCastle.MID - 1,
          MountainCastle.MID + 1,
          z,
          floor,
          GATE_HEIGHT,
          MountainCastle.TRIM);
      for (int x = MountainCastle.MID - 1; x <= MountainCastle.MID + 1; x++) {
        t.set(x, MountainCastle.LOW, z, MountainCastle.FOOT.at(x, MountainCastle.LOW, z));
      }
    }
    for (int x = MountainCastle.MID - 1; x <= MountainCastle.MID + 1; x++) {
      t.set(x, floor + GATE_HEIGHT - 1, SOUTH - 1, "iron_bars");
    }
    t.set(MountainCastle.MID, floor + GATE_HEIGHT - 1, NORTH + 2, Parts.lantern(true));
    for (final int x : new int[] {WEST, EAST}) {
      t.set(
          x,
          MountainCastle.LOW + 4,
          SOUTH + 1,
          BlockState.of(MountainCastle.GARGOYLE, "facing", "south"));
      t.set(x, MountainCastle.LOW + 3, SOUTH + 1, Parts.hood(MountainCastle.TRIM, "north"));
    }
  }

  private static void room(final Template t) {
    final int y = ROOM + 1;
    for (final int x : new int[] {WEST + 2, MountainCastle.MID, EAST - 2}) {
      t.set(x, y + 1, SOUTH, MountainCastle.PANE);
      t.set(x, y + 2, SOUTH, MountainCastle.PANE);
    }
    t.set(WEST, y, NORTH + 3, BlockState.AIR);
    t.set(WEST, y + 1, NORTH + 3, BlockState.AIR);
    t.set(EAST, y, NORTH + 3, BlockState.AIR);
    t.set(EAST, y + 1, NORTH + 3, BlockState.AIR);
    t.set(WEST + 1, y, SOUTH - 1, BlockState.of("nightborn:weapon_rack", "facing", "north"));
    Furnishing.table(t, EAST - 2, y, NORTH + 2);
    Furnishing.chair(t, EAST - 3, y, NORTH + 2, "east");
    Furnishing.candles(t, EAST - 2, y + 1, NORTH + 2, 2, false);
    Furnishing.chest(t, EAST - 1, y, SOUTH - 1, "north", MountainCastle.LOOT);
    t.set(MountainCastle.MID, TOP - 1, NORTH + 4, Parts.lantern(true));
  }
}
