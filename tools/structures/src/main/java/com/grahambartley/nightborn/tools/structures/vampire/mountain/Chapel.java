package com.grahambartley.nightborn.tools.structures.vampire.mountain;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Axis;
import com.grahambartley.nightborn.tools.structures.vampire.Furnishing;
import com.grahambartley.nightborn.tools.structures.vampire.Openings;
import com.grahambartley.nightborn.tools.structures.vampire.Roof;

public final class Chapel {

  public static final int WEST = 42;
  public static final int EAST = 54;
  public static final int NORTH = 18;
  public static final int SOUTH = 36;
  public static final int GROUND = MountainCastle.UP;
  public static final int EAVE = GROUND + 10;
  public static final int MID_X = (WEST + EAST) / 2;
  public static final int DOOR_Z = (NORTH + SOUTH) / 2;
  public static final int[] LANCETS_Z = {NORTH + 3, NORTH + 6, SOUTH - 6, SOUTH - 3};
  public static final int[] PEWS_Z = {NORTH + 7, NORTH + 9, NORTH + 11, NORTH + 13, NORTH + 15};

  private Chapel() {}

  public static void build(final Template t) {
    for (int x = WEST; x <= EAST; x++) {
      for (int z = NORTH; z <= SOUTH; z++) {
        final boolean wall = x == WEST || x == EAST || z == NORTH || z == SOUTH;
        t.set(x, GROUND, z, (x + z) % 2 == 0 ? "deepslate_tiles" : "polished_andesite");
        for (int y = GROUND + 1; y < EAVE; y++) {
          t.set(x, y, z, wall ? MountainCastle.WALL.at(x, y, z) : BlockState.AIR);
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
        EAVE,
        MountainCastle.TILES,
        MountainCastle.WALL,
        true,
        true);
    Openings.arch(t, Axis.Z, DOOR_Z - 1, DOOR_Z + 1, WEST, GROUND + 1, 4, MountainCastle.TRIM);
    for (final int z : LANCETS_Z) {
      Openings.lancet(t, Axis.Z, z, WEST, GROUND + 3, 5, MountainCastle.STAINED);
    }
    for (int dx = -1; dx <= 1; dx++) {
      Openings.lancet(
          t, Axis.X, MID_X + dx, NORTH, GROUND + 4, dx == 0 ? 6 : 4, MountainCastle.STAINED);
    }
    furnish(t);
  }

  private static void furnish(final Template t) {
    final int y = GROUND + 1;
    t.set(MID_X, y, NORTH + 2, "nightborn:carved_gothic_stone");
    Furnishing.chalice(t, MID_X, y + 1, NORTH + 2, "player");
    for (final int dx : new int[] {-2, 2}) {
      Furnishing.candelabra(t, MID_X + dx, y, NORTH + 2);
      Furnishing.drape(t, MountainCastle.BURGUNDY, MID_X + dx * 2, y, NORTH + 1, 6, "south");
    }
    for (int z = NORTH + 4; z < SOUTH; z++) {
      t.set(MID_X, y, z, "red_carpet");
    }
    for (final int z : PEWS_Z) {
      for (int dx = 2; dx <= 4; dx++) {
        Furnishing.chair(t, MID_X - dx, y, z, "north");
        Furnishing.chair(t, MID_X + dx, y, z, "north");
      }
    }
    Furnishing.chest(t, EAST - 1, y, NORTH + 1, "west", MountainCastle.LOOT);
    Furnishing.portrait(t, EAST - 1, y + 2, DOOR_Z, "west");
    t.set(WEST + 1, y, SOUTH - 1, "cobweb");
    t.set(MID_X, y, SOUTH - 2, "nightborn:blood_pool");
  }
}
