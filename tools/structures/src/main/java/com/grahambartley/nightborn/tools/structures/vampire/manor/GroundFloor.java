package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.F0;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.F1;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Furnishing;

public final class GroundFloor {

  public static final int SEAT = F0 + 1;
  public static final int DRAPE_FROM = F0 + 2;
  public static final int CEILING = F1 - 1;

  private GroundFloor() {}

  public static void build(final Template t) {
    drawingRoom(t);
    library(t);
    diningRoom(t);
    kitchen(t);
  }

  private static void drawingRoom(final Template t) {
    for (int x = 15; x <= 19; x++) {
      for (int z = 20; z <= 24; z++) {
        final boolean border = x == 15 || x == 19 || z == 20 || z == 24;
        t.set(x, SEAT, z, border ? "black_carpet" : "red_carpet");
      }
    }
    Furnishing.chair(t, 15, SEAT, 21, "west");
    Furnishing.chair(t, 15, SEAT, 23, "west");
    Furnishing.chair(t, 18, SEAT, 22, "west");
    Furnishing.table(t, 16, SEAT, 22);
    Furnishing.candelabra(t, 16, SEAT + 1, 22);
    Furnishing.chair(t, 16, SEAT, 28, "east");
    Furnishing.table(t, 17, SEAT, 28);
    Furnishing.chalice(t, 17, SEAT + 1, 28, "full");
    Furnishing.chair(t, 18, SEAT, 28, "west");
    Furnishing.portrait(t, 13, CEILING, 22, "east");
    Furnishing.chandelier(t, 17, CEILING, 23);
    for (final int z : new int[] {19, 25}) {
      Furnishing.drape(t, Manor.BURGUNDY, 13, DRAPE_FROM, z, 4, "east");
    }
    t.set(21, SEAT, 18, "potted_fern");
    t.set(21, SEAT, 26, "potted_fern");
    Furnishing.chest(t, 20, SEAT, 26, "north");
    Furnishing.portrait(t, 21, F0 + 3, 22, "west");
  }

  private static void library(final Template t) {
    shelves(t, 13, 14, 9, 9);
    shelves(t, 16, 18, 9, 9);
    shelves(t, 20, 21, 9, 9);
    shelves(t, 21, 21, 13, 16);
    shelves(t, 13, 13, 11, 12);
    shelves(t, 13, 13, 14, 14);
    shelves(t, 13, 16, 16, 16);
    shelves(t, 18, 20, 16, 16);
    for (int y = SEAT; y <= SEAT + 3; y++) {
      t.set(14, y, 10, BlockState.of("ladder", "facing", "south", "waterlogged", false));
    }
    Furnishing.table(t, 17, SEAT, 12);
    Furnishing.table(t, 17, SEAT, 13);
    Furnishing.candelabra(t, 17, SEAT + 1, 12);
    t.set(
        17, SEAT + 1, 13, BlockState.of("candle", "candles", 2, "lit", true, "waterlogged", false));
    Furnishing.table(t, 17, SEAT, 14);
    Furnishing.gramophone(t, 17, SEAT + 1, 14, "west");
    Furnishing.chair(t, 16, SEAT, 12, "east");
    Furnishing.chair(t, 18, SEAT, 13, "west");
    Furnishing.chair(t, 15, SEAT, 10, "south");
    Furnishing.chair(t, 19, SEAT, 10, "south");
    Furnishing.lectern(t, 19, SEAT, 14, "west");
    t.set(13, CEILING, 9, "cobweb");
    t.set(21, CEILING, 16, "cobweb");
  }

  static void shelves(final Template t, final int x0, final int x1, final int z0, final int z1) {
    for (int x = x0; x <= x1; x++) {
      for (int z = z0; z <= z1; z++) {
        for (int y = SEAT; y <= SEAT + 3; y++) {
          t.set(x, y, z, y == SEAT + 1 && (x + z) % 3 == 0 ? "chiseled_bookshelf" : "bookshelf");
        }
      }
    }
  }

  private static void diningRoom(final Template t) {
    for (int z = 17; z <= 20; z++) {
      Furnishing.table(t, 35, SEAT, z);
      Furnishing.chair(t, 34, SEAT, z, "east");
      Furnishing.chair(t, 36, SEAT, z, "west");
    }
    Furnishing.chair(t, 35, SEAT, 16, "south");
    Furnishing.chair(t, 35, SEAT, 21, "north");
    Furnishing.candelabra(t, 35, SEAT + 1, 18);
    Furnishing.candelabra(t, 35, SEAT + 1, 20);
    Furnishing.chalice(t, 35, SEAT + 1, 17, "full");
    Furnishing.chalice(t, 35, SEAT + 1, 19, "full");
    Furnishing.chandelier(t, 35, CEILING, 18);
    for (final int x : new int[] {37, 38}) {
      t.set(x, SEAT, 16, Parts.hood("dark_oak_stairs", "north"));
    }
    Furnishing.chalice(t, 37, SEAT + 1, 16, "empty");
    Furnishing.candles(t, 38, SEAT + 1, 16, 3, false);
    Furnishing.table(t, 41, SEAT, 23);
    Furnishing.chair(t, 40, SEAT, 23, "east");
    Furnishing.chair(t, 41, SEAT, 22, "south");
    for (final int x : new int[] {33, 36}) {
      Furnishing.drape(t, Manor.EMERALD, x, DRAPE_FROM, 21, 4, "north");
    }
    Furnishing.portrait(t, 33, F0 + 3, 17, "east");
  }

  private static void kitchen(final Template t) {
    for (int x = 33; x <= 39; x++) {
      for (int z = 9; z <= 14; z++) {
        t.set(x, F0, z, (x + z) % 2 == 0 ? "polished_andesite" : "tuff_bricks");
      }
    }
    t.set(39, SEAT, 10, BlockState.of("smoker", "facing", "west", "lit", false));
    t.set(39, SEAT, 12, BlockState.of("furnace", "facing", "west", "lit", false));
    t.set(34, SEAT, 9, BlockState.of("water_cauldron", "level", 3));
    t.set(33, SEAT, 13, BlockState.of("barrel", "facing", "up", "open", false));
    t.set(33, SEAT, 14, BlockState.of("barrel", "facing", "up", "open", false));
    t.set(
        33,
        SEAT + 1,
        13,
        BlockState.of("candle", "candles", 1, "lit", false, "waterlogged", false));
    t.set(34, SEAT, 12, "crafting_table");
    t.set(35, SEAT, 12, Parts.log("stripped_spruce_log", "x"));
    t.set(38, SEAT, 9, BlockState.of("barrel", "facing", "west", "open", false));
    Furnishing.chest(t, 38, SEAT, 14, "west");
    t.set(36, CEILING, 11, BlockState.of("chain", "axis", "y", "waterlogged", false));
    t.set(36, CEILING - 1, 11, Parts.lantern(true));
    t.set(39, CEILING, 14, "cobweb");
  }
}
