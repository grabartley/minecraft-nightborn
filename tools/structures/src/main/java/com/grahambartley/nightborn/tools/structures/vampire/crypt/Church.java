package com.grahambartley.nightborn.tools.structures.vampire.crypt;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Axis;
import com.grahambartley.nightborn.tools.structures.vampire.Furnishing;
import com.grahambartley.nightborn.tools.structures.vampire.Openings;
import com.grahambartley.nightborn.tools.structures.vampire.Roof;
import com.grahambartley.nightborn.tools.structures.vampire.Stone;

public final class Church {

  public record Layout(String path, boolean tower) {}

  public static final Layout TOWER = new Layout("vampire/crypt/church_tower", true);
  public static final Layout BELLCOTE = new Layout("vampire/crypt/church_bellcote", false);
  public static final String VESTRY = "nightborn:church_vestry";

  public static final int SIZE_X = 38;
  public static final int SIZE_Y = 23;
  public static final int SIZE_Z = 24;
  public static final int YARD_Z = SIZE_Z - 1;
  public static final int STAIR_X = 27;
  public static final int STAIR_Z = 4;
  public static final int CLEARANCE = 4;

  static final Stone WALL =
      new Stone("stone_bricks", "mossy_stone_bricks", "cracked_stone_bricks", 14);
  static final Stone YARD_WALL =
      new Stone("cobblestone_wall", "mossy_cobblestone_wall", "cobblestone_wall", 40);
  static final Roof.Cover SLATE =
      new Roof.Cover("deepslate_tile_stairs", "deepslate_tiles", "deepslate_tile_slab");
  static final Roof.Cover TIMBER = new Roof.Cover("spruce_stairs", "spruce_planks", "spruce_slab");
  static final String GLASS = "nightborn:gothic_stained_glass_pane";
  static final String TRIM = "stone_brick_stairs";

  private Church() {}

  public static Template build(final Layout layout) {
    final Template t = new Template(layout.path(), SIZE_X, SIZE_Y, SIZE_Z);
    churchyard(t, layout);
    nave(t);
    chancel(t);
    vestry(t);
    porch(t);
    if (layout.tower()) {
      tower(t);
    } else {
      bellcote(t);
    }
    furnish(t);
    lawn(t);
    t.jigsaw(
        STAIR_X,
        0,
        STAIR_Z,
        "down_north",
        VESTRY,
        Template.EMPTY,
        Template.EMPTY,
        CryptStair.step(),
        "aligned");
    return t;
  }

  private static void nave(final Template t) {
    t.fill(8, 0, 5, 22, 0, 13, "polished_andesite");
    WALL.walls(t, 8, 1, 5, 22, 6, 13);
    t.fill(9, 1, 6, 21, 10, 12, "air");
    Roof.gable(t, Axis.X, 8, 22, 5, 13, 6, SLATE, WALL);
    for (final int x : new int[] {15, 19}) {
      buttress(t, x, 4, "south");
      buttress(t, x, 14, "north");
    }
    buttress(t, 11, 4, "south");
    for (final int x : new int[] {10, 17, 21}) {
      Openings.lancet(t, Axis.X, x, 13, 2, 3, GLASS);
    }
    for (final int x : new int[] {9, 13, 17, 21}) {
      Openings.lancet(t, Axis.X, x, 5, 2, 3, GLASS);
    }
    Openings.arch(t, Axis.X, 13, 13, 13, 1, 2, TRIM);
  }

  private static void chancel(final Template t) {
    t.fill(23, 0, 6, 30, 0, 12, "stone_bricks");
    t.set(25, 0, 9, "chiseled_stone_bricks");
    t.set(27, 0, 9, "chiseled_stone_bricks");
    WALL.walls(t, 22, 1, 6, 30, 5, 12);
    t.fill(23, 1, 7, 29, 8, 11, "air");
    Roof.gable(t, Axis.X, 23, 30, 6, 12, 5, SLATE, WALL);
    Openings.arch(t, Axis.Z, 8, 10, 22, 1, 4, TRIM);
    Openings.lancet(t, Axis.Z, 7, 30, 2, 3, GLASS);
    Openings.lancet(t, Axis.Z, 9, 30, 2, 5, GLASS);
    Openings.lancet(t, Axis.Z, 11, 30, 2, 3, GLASS);
    for (final int x : new int[] {25, 28}) {
      Openings.lancet(t, Axis.X, x, 12, 2, 3, GLASS);
    }
    Openings.lancet(t, Axis.X, 29, 6, 2, 3, GLASS);
  }

  private static void vestry(final Template t) {
    t.fill(24, 0, 2, 28, 0, 5, "stone_bricks");
    WALL.fill(t, 24, 1, 2, 28, 3, 2);
    WALL.fill(t, 24, 1, 2, 24, 4, 5);
    WALL.fill(t, 28, 1, 2, 28, 4, 5);
    t.fill(25, 1, 3, 27, 3, 5, "air");
    t.fill(25, 4, 4, 27, 4, 5, "air");
    for (int x = 24; x <= 28; x++) {
      t.set(x, 4, 1, stair("deepslate_tile_stairs", "south"));
      t.set(x, 4, 2, "deepslate_tiles");
      t.set(x, 4, 3, "deepslate_tiles");
      t.set(x, 5, 4, stair("deepslate_tile_stairs", "south"));
    }
    t.set(26, 1, 6, door("north", "lower"));
    t.set(26, 2, 6, door("north", "upper"));
    t.fill(STAIR_X - 3, 0, STAIR_Z, STAIR_X - 1, 0, STAIR_Z, "air");
    t.set(25, 1, 3, "brewing_stand");
    t.set(25, 1, 5, BlockState.of("barrel", "facing", "up", "open", false));
    t.set(27, 3, 3, BlockState.of("lantern", "hanging", false, "waterlogged", false));
  }

  private static void porch(final Template t) {
    t.fill(12, 0, 14, 14, 0, 16, "stone_bricks");
    WALL.fill(t, 12, 1, 14, 12, 3, 16);
    WALL.fill(t, 14, 1, 14, 14, 3, 16);
    t.fill(13, 1, 14, 13, 3, 16, "air");
    for (int z = 14; z <= 17; z++) {
      t.set(12, 4, z, stair("deepslate_tile_stairs", "east"));
      t.set(14, 4, z, stair("deepslate_tile_stairs", "west"));
      t.set(13, 4, z, "deepslate_tiles");
      t.set(13, 5, z, BlockState.of("deepslate_tile_slab", "type", "bottom"));
    }
    t.set(13, 3, 16, WALL.at(13, 3, 16));
  }

  private static void tower(final Template t) {
    t.fill(3, 0, 7, 7, 0, 11, "stone_bricks");
    WALL.walls(t, 3, 1, 7, 7, 18, 11);
    t.fill(4, 1, 8, 6, 17, 10, "air");
    t.fill(4, 13, 8, 6, 13, 10, "spruce_planks");
    t.fill(4, 18, 8, 6, 18, 10, "stone_bricks");
    for (int y = 1; y <= 13; y++) {
      t.set(4, y, 8, BlockState.of("ladder", "facing", "east", "waterlogged", false));
    }
    WALL.walls(t, 3, 19, 7, 7, 19, 11);
    for (final int[] corner : new int[][] {{3, 7}, {3, 11}, {7, 7}, {7, 11}}) {
      t.set(corner[0], 20, corner[1], "stone_brick_wall");
      t.set(corner[0], 21, corner[1], "stone_brick_wall");
    }
    for (final int[] merlon : new int[][] {{5, 7}, {5, 11}, {3, 9}, {7, 9}}) {
      t.set(merlon[0], 20, merlon[1], "stone_bricks");
    }
    Openings.arch(t, Axis.Z, 8, 10, 7, 1, 4, TRIM);
    Openings.arch(t, Axis.Z, 8, 10, 8, 1, 4, TRIM);
    Openings.lancet(t, Axis.Z, 9, 3, 4, 4, GLASS);
    Openings.arch(t, Axis.X, 5, 5, 7, 14, 3, TRIM);
    Openings.arch(t, Axis.X, 5, 5, 11, 14, 3, TRIM);
    Openings.arch(t, Axis.Z, 9, 9, 3, 14, 3, TRIM);
    Openings.arch(t, Axis.Z, 9, 9, 7, 14, 3, TRIM);
    t.set(
        5,
        17,
        9,
        BlockState.of("bell", "attachment", "ceiling", "facing", "north", "powered", false));
  }

  private static void bellcote(final Template t) {
    WALL.fill(t, 8, 11, 8, 8, 15, 8);
    WALL.fill(t, 8, 11, 10, 8, 15, 10);
    WALL.fill(t, 8, 15, 9, 8, 15, 9);
    t.set(8, 12, 9, WALL.at(8, 12, 9));
    t.set(8, 16, 9, BlockState.of("stone_brick_slab", "type", "bottom"));
    t.set(
        8,
        14,
        9,
        BlockState.of("bell", "attachment", "ceiling", "facing", "east", "powered", false));
    t.set(8, 13, 9, "air");
    Openings.arch(t, Axis.Z, 8, 10, 8, 1, 3, TRIM);
    Openings.lancet(t, Axis.Z, 9, 8, 5, 4, GLASS);
    t.fill(5, 0, 9, 7, 0, 9, "dirt_path");
    t.fill(6, 0, 10, 6, 0, 20, "dirt_path");
    t.fill(7, 0, 20, 12, 0, 20, "dirt_path");
  }

  private static void buttress(final Template t, final int x, final int z, final String slope) {
    WALL.fill(t, x, 1, z, x, 3, z);
    t.set(x, 4, z, stair(TRIM, slope));
  }

  private static void furnish(final Template t) {
    for (int x = 11; x <= 19; x += 2) {
      for (final int z : new int[] {7, 8, 10, 11}) {
        t.set(x, 1, z, stair("spruce_stairs", "west"));
      }
    }
    t.set(10, 1, 11, BlockState.of("water_cauldron", "level", 3));
    t.set(
        21, 1, 7, BlockState.of("lectern", "facing", "west", "has_book", false, "powered", false));
    for (final int x : new int[] {12, 18}) {
      for (int y = 6; y <= 10; y++) {
        t.set(x, y, 9, BlockState.of("chain", "axis", "y", "waterlogged", false));
      }
      t.set(x, 5, 9, BlockState.of("lantern", "hanging", true, "waterlogged", false));
    }
    for (int x = 23; x <= 25; x++) {
      t.set(x, 1, 7, stair("spruce_stairs", "north"));
      t.set(x, 1, 11, stair("spruce_stairs", "south"));
    }
    t.set(29, 1, 8, "polished_andesite");
    t.set(29, 1, 9, "chiseled_stone_bricks");
    t.set(29, 1, 10, "polished_andesite");
    Furnishing.candles(t, 29, 2, 8, 2, true);
    Furnishing.candles(t, 29, 2, 10, 2, true);
  }

  private static void churchyard(final Template t, final Layout layout) {
    for (int x = 0; x < SIZE_X; x++) {
      wallAt(t, x, 0);
      if (x < 12 || x > 14) {
        wallAt(t, x, 22);
      }
    }
    for (int z = 1; z < 22; z++) {
      wallAt(t, 0, z);
      wallAt(t, SIZE_X - 1, z);
    }
    lychgate(t);
    for (int z = 17; z <= 22; z++) {
      t.set(13, 0, z, "dirt_path");
    }
    for (int x = 18; x <= 34; x += 4) {
      grave(t, x, 16);
      grave(t, x, 19);
    }
    for (final int z : new int[] {7, 11}) {
      grave(t, 32, z);
    }
    chestTomb(t, 9, 17);
    if (layout.tower()) {
      yew(t, 4, 17);
    } else {
      for (final int z : new int[] {3, 13, 16}) {
        grave(t, 2, z);
      }
      yew(t, 33, 3);
    }
  }

  private static void lawn(final Template t) {
    for (int x = 0; x < SIZE_X; x++) {
      for (int z = 0; z < YARD_Z; z++) {
        if (t.get(x, 0, z) == null) {
          t.set(x, 0, z, BlockState.of("grass_block", "snowy", false));
        }
        for (int y = 1; y <= CLEARANCE; y++) {
          if (t.get(x, y, z) == null) {
            t.set(x, y, z, BlockState.AIR);
          }
        }
      }
    }
  }

  private static void wallAt(final Template t, final int x, final int z) {
    t.set(x, 1, z, YARD_WALL.at(x, 1, z));
  }

  private static void lychgate(final Template t) {
    for (final int x : new int[] {11, 15}) {
      t.fill(x, 1, 22, x, 3, 22, BlockState.of("dark_oak_log", "axis", "y"));
    }
    for (int x = 10; x <= 16; x++) {
      t.set(x, 4, 21, stair(TIMBER.stairs(), "south"));
      t.set(x, 4, 23, stair(TIMBER.stairs(), "north"));
      t.set(x, 4, 22, TIMBER.full());
      t.set(x, 5, 22, BlockState.of(TIMBER.slab(), "type", "bottom"));
    }
  }

  private static void grave(final Template t, final int x, final int z) {
    t.set(x, 1, z, Stone.roll(x, 1, z) < 50 ? "stone_brick_wall" : "mossy_stone_brick_wall");
    t.set(x + 1, 0, z, "podzol");
    t.set(x + 2, 0, z, "coarse_dirt");
  }

  private static void chestTomb(final Template t, final int x, final int z) {
    t.set(x, 1, z, "polished_andesite");
    t.set(x + 1, 1, z, "polished_andesite");
    t.set(x, 2, z, BlockState.of("smooth_stone_slab", "type", "bottom"));
    t.set(x + 1, 2, z, BlockState.of("smooth_stone_slab", "type", "bottom"));
  }

  private static void yew(final Template t, final int x, final int z) {
    for (int y = 1; y <= 3; y++) {
      t.set(x, y, z, BlockState.of("spruce_log", "axis", "y"));
    }
    for (int dx = -2; dx <= 2; dx++) {
      for (int dz = -2; dz <= 2; dz++) {
        for (int y = 3; y <= 6; y++) {
          final int reach = y == 3 || y == 6 ? 1 : 2;
          if (Math.abs(dx) <= reach
              && Math.abs(dz) <= reach
              && !(dx == 0 && dz == 0 && y < 4)
              && Math.abs(dx) + Math.abs(dz) < reach + 2) {
            t.set(
                x + dx,
                y,
                z + dz,
                BlockState.of(
                    "spruce_leaves", "distance", 1, "persistent", true, "waterlogged", false));
          }
        }
      }
    }
  }

  static String stair(final String block, final String facing) {
    return BlockState.of(
        block, "facing", facing, "half", "bottom", "shape", "straight", "waterlogged", false);
  }

  static String door(final String facing, final String half) {
    return BlockState.of(
        "spruce_door",
        "facing",
        facing,
        "half",
        half,
        "hinge",
        "left",
        "open",
        true,
        "powered",
        false);
  }
}
