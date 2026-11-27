package com.grahambartley.nightborn.tools.structures.vampire.mountain;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Axis;
import com.grahambartley.nightborn.tools.structures.vampire.Furnishing;
import com.grahambartley.nightborn.tools.structures.vampire.Roof;
import com.grahambartley.nightborn.tools.structures.vampire.RouteRoom;
import com.grahambartley.nightborn.tools.structures.vampire.manor.Parts;

public final class HallWing {

  public static final int WEST = 8;
  public static final int EAST = 20;
  public static final int NORTH = 14;
  public static final int SOUTH = 36;
  public static final int GROUND = MountainCastle.UP;
  public static final int LOFT = GROUND + 6;
  public static final int PLATE = LOFT + 5;
  public static final int BAY = 3;
  public static final int DOOR_Z = 25;
  public static final int TABLE_X = 14;

  private HallWing() {}

  public static boolean onWall(final int x, final int z) {
    return x == WEST || x == EAST || z == NORTH || z == SOUTH;
  }

  public static boolean post(final int x, final int z) {
    final boolean corner = (x == WEST || x == EAST) && (z == NORTH || z == SOUTH);
    final int along = x == WEST || x == EAST ? z - NORTH : x - WEST;
    return corner || along % BAY == 0;
  }

  static String frame(final int x, final int y, final int z) {
    if (y == LOFT || y == PLATE) {
      return Parts.log(MountainCastle.TIMBER, x == WEST || x == EAST ? "z" : "x");
    }
    return post(x, z) ? Parts.log(MountainCastle.TIMBER, "y") : MountainCastle.PLASTER.base();
  }

  public static void build(final Template t) {
    for (int x = WEST; x <= EAST; x++) {
      for (int z = NORTH; z <= SOUTH; z++) {
        t.set(x, GROUND, z, MountainCastle.PLANKS);
        for (int y = GROUND + 1; y <= PLATE; y++) {
          if (onWall(x, z)) {
            t.set(x, y, z, y < LOFT ? MountainCastle.WALL.at(x, y, z) : frame(x, y, z));
          } else {
            t.set(x, y, z, y == LOFT ? MountainCastle.PLANKS : BlockState.AIR);
          }
        }
      }
    }
    Roof.gable(
        t,
        Axis.Z,
        NORTH,
        SOUTH,
        WEST,
        EAST,
        PLATE + 1,
        MountainCastle.TILES,
        MountainCastle.PLASTER,
        true,
        true);
    openings(t);
    flight(t);
    feast(t);
    library(t);
  }

  private static void openings(final Template t) {
    for (int dz = 0; dz < RouteRoom.LANES; dz++) {
      for (int y = GROUND + 1; y <= GROUND + RouteRoom.HOUSEHOLD; y++) {
        t.set(EAST, y, DOOR_Z + dz, BlockState.AIR);
      }
    }
    for (int z = NORTH + 1; z < SOUTH; z++) {
      if ((z - NORTH) % BAY != 1) {
        continue;
      }
      if (z != DOOR_Z && z != DOOR_Z + 1) {
        t.set(EAST, GROUND + 2, z, MountainCastle.PANE);
        t.set(EAST, GROUND + 3, z, MountainCastle.PANE);
      }
      t.set(EAST, LOFT + 2, z, MountainCastle.PANE);
      t.set(EAST, LOFT + 3, z, MountainCastle.PANE);
    }
    for (int x = WEST + 1; x < EAST; x++) {
      if ((x - WEST) % BAY == 1) {
        t.set(x, LOFT + 2, SOUTH, MountainCastle.PANE);
        t.set(x, LOFT + 3, SOUTH, MountainCastle.PANE);
        t.set(x, LOFT + 2, NORTH, MountainCastle.PANE);
        t.set(x, LOFT + 3, NORTH, MountainCastle.PANE);
      }
    }
  }

  private static void flight(final Template t) {
    final int steps = LOFT - GROUND;
    for (int z = NORTH + 1; z <= NORTH + RouteRoom.LANES; z++) {
      for (int i = 0; i < steps; i++) {
        final int x = WEST + 2 + i;
        final int y = GROUND + 1 + i;
        t.set(x, y, z, Parts.stair("dark_oak_stairs", "east"));
        for (int above = y + 1; above <= y + 1 + RouteRoom.HOUSEHOLD; above++) {
          t.set(x, above, z, BlockState.AIR);
        }
      }
      t.set(WEST + 2, LOFT + 1, z, Parts.fence());
    }
    for (int x = WEST + 3; x <= WEST + steps; x++) {
      t.set(x, LOFT + 1, NORTH + RouteRoom.LANES + 1, Parts.fence());
    }
  }

  private static void feast(final Template t) {
    final int y = GROUND + 1;
    for (int z = NORTH + 6; z <= SOUTH - 5; z++) {
      Furnishing.table(t, TABLE_X, y, z);
      if (z % 2 == 0) {
        Furnishing.chair(t, TABLE_X - 1, y, z, "east");
        Furnishing.chair(t, TABLE_X + 1, y, z, "west");
      }
    }
    Furnishing.chalice(t, TABLE_X, y + 1, NORTH + 7, "villager");
    Furnishing.candles(t, TABLE_X, y + 1, NORTH + 11, 4, false);
    Furnishing.chalice(t, TABLE_X, y + 1, SOUTH - 6, "villager");
    for (final int z : new int[] {NORTH + 8, SOUTH - 8}) {
      Furnishing.chandelier(t, TABLE_X, LOFT - 1, z);
      t.set(WEST + 1, y + 2, z, BlockState.of("nightborn:trophy_mount", "facing", "east"));
    }
    Furnishing.drape(t, MountainCastle.EMERALD, WEST + 1, y, NORTH + 12, 4, "east");
    Furnishing.portrait(t, TABLE_X, y + 2, SOUTH - 1, "north");
    Furnishing.candelabra(t, WEST + 1, y, SOUTH - 1);
    Furnishing.chest(t, EAST - 1, y, SOUTH - 1, "north", MountainCastle.LOOT);
    t.set(EAST - 1, LOFT - 1, NORTH + 1, "cobweb");
  }

  private static void library(final Template t) {
    final int y = LOFT + 1;
    for (int z = NORTH + 5; z <= SOUTH - 2; z++) {
      for (int dy = 0; dy < 3; dy++) {
        t.set(WEST + 1, y + dy, z, "bookshelf");
      }
    }
    for (final int z : new int[] {NORTH + 9, SOUTH - 6}) {
      Furnishing.lectern(t, WEST + 4, y, z, "west");
      Furnishing.table(t, EAST - 3, y, z);
      Furnishing.chair(t, EAST - 4, y, z, "east");
      Furnishing.candelabra(t, EAST - 3, y + 1, z);
    }
    Furnishing.chest(t, EAST - 1, y, SOUTH - 1, "west", MountainCastle.LOOT);
    t.set(WEST + 1, y + 3, NORTH + 5, "cobweb");
  }
}
