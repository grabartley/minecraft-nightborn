package com.grahambartley.nightborn.tools.structures.vampire.castle;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Axis;
import com.grahambartley.nightborn.tools.structures.vampire.Furnishing;
import com.grahambartley.nightborn.tools.structures.vampire.Openings;
import com.grahambartley.nightborn.tools.structures.vampire.Roof;

public final class Keep {

  public static final int WEST = 12;
  public static final int EAST = 28;
  public static final int NORTH = 6;
  public static final int SOUTH = 30;
  public static final int SCREEN = 17;
  public static final int CEILING = 21;
  public static final int SECOND = 8;
  public static final int THIRD = 14;
  public static final int THRONE_Z = Castle.MID - 2;
  public static final int DAIS_FRONT = Castle.MID + 1;
  public static final int[] COLUMNS_Z = {21, 25, 29};
  public static final int[] HALL_WINDOWS_Z = {19, 23, 27};

  static final String SHAFT = "deepslate_brick_wall";
  static final String CAPITAL = "chiseled_deepslate";
  static final String DAIS = "polished_blackstone_bricks";

  private Keep() {}

  public static void build(final Template t) {
    shell(t);
    hallFloor(t);
    northFloors(t);
    stairs(t);
    windows(t);
    hall(t);
    crypt(t);
    bedchamber(t);
    library(t);
    Roof.gable(t, Axis.Z, NORTH, SOUTH, WEST, EAST, CEILING + 1, Castle.ROOF, Castle.WALL);
    for (final int[] corner :
        new int[][] {
          {WEST - 1, NORTH - 1}, {EAST + 1, NORTH - 1}, {WEST - 1, SOUTH + 1}, {EAST + 1, SOUTH + 1}
        }) {
      new Tower(corner[0], corner[1], 2, CEILING + 4, true).build(t, Castle.WALL, CEILING);
    }
  }

  private static void shell(final Template t) {
    for (int x = WEST; x <= EAST; x++) {
      for (int z = NORTH; z <= SOUTH; z++) {
        final boolean outer = x == WEST || x == EAST || z == NORTH || z == SOUTH;
        for (int y = 0; y <= CEILING; y++) {
          if (y < Castle.FLOOR) {
            t.set(x, y, z, Castle.PLINTH.at(x, y, z));
          } else if (outer || z == SCREEN) {
            t.set(x, y, z, Castle.WALL.at(x, y, z));
          } else if (y == CEILING) {
            t.set(x, y, z, Castle.PLANKS);
          } else {
            t.set(x, y, z, BlockState.AIR);
          }
        }
      }
    }
    for (final int z : new int[] {NORTH + 4, SCREEN, 21, 25}) {
      buttress(t, WEST - 1, z, "east");
      buttress(t, EAST + 1, z, "west");
    }
    Openings.arch(t, Axis.X, Castle.MID - 1, Castle.MID + 1, SOUTH, Castle.FLOOR, 4, Castle.STEP);
    for (final int x : new int[] {WEST + 2, EAST - 2}) {
      t.set(x, Castle.FLOOR, SCREEN, BlockState.AIR);
      t.set(x, Castle.FLOOR + 1, SCREEN, BlockState.AIR);
    }
  }

  private static void buttress(final Template t, final int x, final int z, final String slope) {
    for (int y = 0; y < 12; y++) {
      t.set(x, y, z, y < Castle.FLOOR ? Castle.PLINTH.at(x, y, z) : Castle.WALL.at(x, y, z));
    }
    t.set(
        x,
        12,
        z,
        BlockState.of(
            Castle.STEP,
            "facing",
            slope,
            "half",
            "bottom",
            "shape",
            "straight",
            "waterlogged",
            false));
  }

  private static void hallFloor(final Template t) {
    for (int x = WEST + 1; x < EAST; x++) {
      for (int z = NORTH + 1; z < SOUTH; z++) {
        t.set(x, Castle.GROUND, z, (x + z) % 2 == 0 ? "polished_deepslate" : "deepslate_tiles");
      }
    }
  }

  private static void northFloors(final Template t) {
    for (final int y : new int[] {SECOND, THIRD}) {
      t.fill(WEST + 1, y, NORTH + 1, EAST - 1, y, SCREEN - 1, Castle.PLANKS);
    }
  }

  private static void stairs(final Template t) {
    flight(t, WEST + 1, Castle.FLOOR, SECOND);
    flight(t, EAST - 2, SECOND + 1, THIRD);
  }

  private static void flight(final Template t, final int x, final int fromY, final int floorY) {
    final int steps = floorY - fromY + 1;
    for (int i = 0; i < steps; i++) {
      final int z = SCREEN - 2 - i;
      for (int dx = 0; dx < 2; dx++) {
        t.set(
            x + dx,
            fromY + i,
            z,
            BlockState.of(
                Castle.STEP,
                "facing",
                "north",
                "half",
                "bottom",
                "shape",
                "straight",
                "waterlogged",
                false));
        for (int y = fromY + i + 1; y <= floorY; y++) {
          t.set(x + dx, y, z, BlockState.AIR);
        }
      }
    }
  }

  private static void windows(final Template t) {
    for (final int z : HALL_WINDOWS_Z) {
      for (final int x : new int[] {WEST, EAST}) {
        Openings.lancet(t, Axis.Z, z, x, Castle.FLOOR + 4, 8, Castle.GLASS);
      }
    }
    for (final int y : new int[] {Castle.FLOOR + 2, SECOND + 2, THIRD + 2}) {
      for (final int z : new int[] {NORTH + 3, NORTH + 7}) {
        Openings.lancet(t, Axis.Z, z, WEST, y, 2, Castle.GLASS);
        Openings.lancet(t, Axis.Z, z, EAST, y, 2, Castle.GLASS);
      }
      Openings.lancet(t, Axis.X, Castle.MID, NORTH, y, 2, Castle.GLASS);
    }
    for (int dx = -2; dx <= 2; dx++) {
      for (int dy = -2; dy <= 2; dy++) {
        if (dx * dx + dy * dy <= 5) {
          t.set(Castle.MID + dx, 14 + dy, SOUTH, Castle.GLASS);
        }
      }
    }
  }

  private static void hall(final Template t) {
    t.fill(
        Castle.MID - 4, Castle.FLOOR, SCREEN + 1, Castle.MID + 4, Castle.FLOOR, DAIS_FRONT, DAIS);
    for (int x = Castle.MID - 3; x <= Castle.MID + 3; x++) {
      t.set(
          x,
          Castle.FLOOR,
          DAIS_FRONT + 1,
          BlockState.of(
              Castle.STEP,
              "facing",
              "north",
              "half",
              "bottom",
              "shape",
              "straight",
              "waterlogged",
              false));
    }
    Furnishing.chair(t, Castle.MID, Castle.FLOOR + 1, THRONE_Z, "south");
    Furnishing.candelabra(t, Castle.MID - 2, Castle.FLOOR + 1, THRONE_Z);
    Furnishing.candelabra(t, Castle.MID + 2, Castle.FLOOR + 1, THRONE_Z);
    Furnishing.standingCoffin(
        t, Castle.GOTHIC_COFFIN, Castle.MID - 4, Castle.FLOOR + 1, SCREEN + 1, "north");
    Furnishing.standingCoffin(
        t, Castle.GOTHIC_COFFIN, Castle.MID + 4, Castle.FLOOR + 1, SCREEN + 1, "north");
    Furnishing.drape(
        t,
        "nightborn:burgundy_velvet_drape",
        Castle.MID - 1,
        Castle.FLOOR + 2,
        SCREEN + 1,
        6,
        "south");
    Furnishing.drape(
        t,
        "nightborn:burgundy_velvet_drape",
        Castle.MID + 1,
        Castle.FLOOR + 2,
        SCREEN + 1,
        6,
        "south");
    for (int z = DAIS_FRONT + 2; z < SOUTH; z++) {
      t.set(Castle.MID, Castle.FLOOR, z, "red_carpet");
    }
    for (final int z : COLUMNS_Z) {
      for (final int x : new int[] {Castle.MID - 5, Castle.MID + 5}) {
        t.set(x, Castle.FLOOR, z, CAPITAL);
        for (int y = Castle.FLOOR + 1; y < CEILING - 1; y++) {
          t.set(
              x,
              y,
              z,
              BlockState.of(
                  SHAFT,
                  "up",
                  true,
                  "north",
                  "none",
                  "south",
                  "none",
                  "east",
                  "none",
                  "west",
                  "none",
                  "waterlogged",
                  false));
        }
        t.set(x, CEILING - 1, z, CAPITAL);
      }
      Furnishing.chandelier(t, Castle.MID, CEILING - 1, z);
    }
    for (final int x : new int[] {Castle.MID - 3, Castle.MID + 3}) {
      for (int z = 24; z <= 27; z++) {
        Furnishing.table(t, x, Castle.FLOOR, z);
      }
      Furnishing.chalice(t, x, Castle.FLOOR + 1, 25, "villager");
      Furnishing.candles(t, x, Castle.FLOOR + 1, 27, 3, false);
    }
    Furnishing.gramophone(t, Castle.MID - 3, Castle.FLOOR + 1, 26, "east");
    for (int z = 24; z <= 27; z++) {
      Furnishing.chair(t, Castle.MID - 4, Castle.FLOOR, z, "east");
      Furnishing.chair(t, Castle.MID + 4, Castle.FLOOR, z, "west");
    }
    for (final int z : new int[] {21, 28}) {
      t.set(
          WEST + 1, Castle.FLOOR + 2, z, BlockState.of("nightborn:trophy_mount", "facing", "east"));
      t.set(
          EAST - 1, Castle.FLOOR + 2, z, BlockState.of("nightborn:trophy_mount", "facing", "west"));
    }
    t.set(Castle.MID, Castle.FLOOR, SOUTH - 2, "nightborn:blood_pool");
  }

  private static void crypt(final Template t) {
    final int y = Castle.FLOOR;
    Furnishing.coffin(t, Castle.GOTHIC_COFFIN, Castle.MID - 3, y, NORTH + 3, "north");
    Furnishing.coffin(t, Castle.GOTHIC_COFFIN, Castle.MID + 3, y, NORTH + 3, "north");
    t.set(Castle.MID, y, NORTH + 2, "nightborn:carved_gothic_stone");
    Furnishing.chalice(t, Castle.MID, y + 1, NORTH + 2, "player");
    Furnishing.candelabra(t, Castle.MID - 1, y, NORTH + 1);
    Furnishing.candelabra(t, Castle.MID + 1, y, NORTH + 1);
    Furnishing.oldBloodFont(t, Castle.MID, y, NORTH + 5);
    Furnishing.chest(t, EAST - 1, y, NORTH + 5, "west", Castle.LOOT);
    t.set(WEST + 4, y, NORTH + 1, "cobweb");
    t.set(EAST - 4, SECOND - 1, NORTH + 1, "cobweb");
    t.set(Castle.MID + 5, y, SCREEN - 2, BlockState.of("skeleton_skull", "rotation", 6));
  }

  private static void bedchamber(final Template t) {
    final int y = SECOND + 1;
    for (final int x : new int[] {Castle.MID - 4, Castle.MID - 1, Castle.MID + 2}) {
      Furnishing.coffin(t, Castle.DARK_OAK_COFFIN, x, y, NORTH + 3, "north");
    }
    Furnishing.coffin(t, Castle.DARK_OAK_COFFIN, Castle.MID + 5, y, NORTH + 3, "north");
    Furnishing.drape(t, "nightborn:emerald_velvet_drape", Castle.MID - 1, y, NORTH + 1, 3, "south");
    Furnishing.drape(t, "nightborn:emerald_velvet_drape", Castle.MID + 1, y, NORTH + 1, 3, "south");
    Furnishing.candles(t, Castle.MID, y, NORTH + 6, 4, false);
    Furnishing.chest(t, WEST + 1, y, SCREEN - 1, "east", Castle.LOOT);
    Furnishing.chandelier(t, Castle.MID, THIRD - 1, NORTH + 6);
  }

  private static void library(final Template t) {
    final int y = THIRD + 1;
    for (int x = WEST + 1; x <= EAST - 1; x++) {
      for (int dy = 0; dy < 3; dy++) {
        t.set(x, y + dy, NORTH + 1, "bookshelf");
      }
    }
    t.set(
        Castle.MID,
        y,
        NORTH + 5,
        BlockState.of("lectern", "facing", "south", "has_book", false, "powered", false));
    Furnishing.table(t, Castle.MID - 3, y, NORTH + 7);
    Furnishing.chair(t, Castle.MID - 4, y, NORTH + 7, "east");
    Furnishing.candelabra(t, Castle.MID - 3, y + 1, NORTH + 7);
    Furnishing.chest(t, WEST + 3, y, SCREEN - 1, "north", Castle.LOOT);
    Furnishing.chandelier(t, Castle.MID, CEILING - 1, NORTH + 6);
    t.set(EAST - 4, y, NORTH + 2, "cobweb");
  }
}
