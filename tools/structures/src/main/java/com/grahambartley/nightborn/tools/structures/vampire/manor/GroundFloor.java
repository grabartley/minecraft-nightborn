package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.F0;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.F1;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Furnishing;

public final class GroundFloor {

  public static final int SEAT = F0 + 1;
  public static final int CEILING = F1 - 1;
  public static final int SHELF_TOP = SEAT + 4;

  private GroundFloor() {}

  public static void build(final Template t) {
    drawingRoom(t);
    library(t);
    diningRoom(t);
    kitchen(t);
  }

  private static void drawingRoom(final Template t) {
    for (int x = 22; x <= 28; x++) {
      for (int z = 31; z <= 39; z++) {
        final boolean border = x == 22 || x == 28 || z == 31 || z == 39;
        t.set(x, SEAT, z, border ? "black_carpet" : "red_carpet");
      }
    }
    Furnishing.chair(t, 22, SEAT, 32, "west");
    Furnishing.chair(t, 22, SEAT, 36, "west");
    Furnishing.table(t, 24, SEAT, 34);
    Furnishing.candelabra(t, 24, SEAT + 1, 34);
    Furnishing.chair(t, 24, SEAT, 33, "south");
    Furnishing.chair(t, 24, SEAT, 35, "north");
    Furnishing.chair(t, 25, SEAT, 34, "west");
    Furnishing.table(t, 29, SEAT, 31);
    Furnishing.gramophone(t, 29, SEAT + 1, 31, "west");
    Furnishing.chair(t, 28, SEAT, 31, "east");
    Furnishing.chest(t, 21, SEAT, 40, "north");
    Furnishing.portrait(t, 25, F0 + 3, 40, "north");
    Furnishing.portrait(t, 28, F0 + 3, 30, "south");
    Furnishing.portrait(t, 21, F0 + 3, 30, "south");
    Furnishing.chandelier(t, 25, CEILING, 35);
    t.set(30, SEAT, 40, "potted_fern");
    t.set(30, SEAT, 31, "potted_fern");
  }

  private static void library(final Template t) {
    shelves(t, 20, 21, 18, 18);
    shelves(t, 24, 26, 18, 18);
    shelves(t, 29, 30, 18, 18);
    shelves(t, 20, 20, 19, 20);
    shelves(t, 20, 20, 25, 28);
    shelves(t, 30, 30, 19, 22);
    shelves(t, 21, 23, 28, 28);
    shelves(t, 26, 30, 28, 28);
    Furnishing.table(t, 25, SEAT, 23);
    Furnishing.table(t, 25, SEAT, 24);
    Furnishing.candelabra(t, 25, SEAT + 1, 23);
    Furnishing.candles(t, 25, SEAT + 1, 24, 2, false);
    Furnishing.chair(t, 24, SEAT, 23, "east");
    Furnishing.chair(t, 26, SEAT, 24, "west");
    Furnishing.lectern(t, 28, SEAT, 21, "south");
    Furnishing.chair(t, 28, SEAT, 23, "north");
    Furnishing.chest(t, 21, SEAT, 21, "east");
    Furnishing.chandelier(t, 25, CEILING, 23);
    t.set(20, CEILING, 18, "cobweb");
    t.set(30, CEILING, 28, "cobweb");
  }

  static void shelves(final Template t, final int x0, final int x1, final int z0, final int z1) {
    for (int x = x0; x <= x1; x++) {
      for (int z = z0; z <= z1; z++) {
        for (int y = SEAT; y <= SHELF_TOP; y++) {
          t.set(x, y, z, y == SEAT + 1 && (x + z) % 3 == 0 ? "chiseled_bookshelf" : "bookshelf");
        }
      }
    }
  }

  private static void diningRoom(final Template t) {
    for (int x = 48; x <= 53; x++) {
      Furnishing.table(t, x, SEAT, 35);
      Furnishing.chair(t, x, SEAT, 34, "south");
      Furnishing.chair(t, x, SEAT, 36, "north");
    }
    Furnishing.chair(t, 47, SEAT, 35, "east");
    Furnishing.chair(t, 54, SEAT, 35, "west");
    Furnishing.candelabra(t, 49, SEAT + 1, 35);
    Furnishing.candelabra(t, 52, SEAT + 1, 35);
    Furnishing.chalice(t, 50, SEAT + 1, 35, "full");
    Furnishing.chalice(t, 51, SEAT + 1, 35, "full");
    Furnishing.chandelier(t, 50, CEILING, 35);
    for (final int x : new int[] {47, 48, 53, 54}) {
      t.set(x, SEAT, 30, Parts.hood("dark_oak_stairs", "north"));
    }
    Furnishing.chalice(t, 47, SEAT + 1, 30, "empty");
    Furnishing.candles(t, 54, SEAT + 1, 30, 3, false);
    Furnishing.portrait(t, 47, F0 + 3, 30, "south");
    Furnishing.portrait(t, 54, F0 + 3, 30, "south");
    Furnishing.table(t, Turret.CX + 1, SEAT, Turret.CZ);
    Furnishing.candles(t, Turret.CX + 1, SEAT + 1, Turret.CZ, 3, false);
    Furnishing.chair(t, Turret.CX + 1, SEAT, Turret.CZ + 1, "north");
    Furnishing.chair(t, Turret.CX, SEAT, Turret.CZ, "east");
  }

  private static void kitchen(final Template t) {
    final ManorShell.Box room = ManorShell.KITCHEN;
    for (int x = room.x0(); x <= room.x1(); x++) {
      for (int z = room.z0(); z <= room.z1(); z++) {
        if (Manor.PLANKS.equals(t.get(x, F0, z))) {
          t.set(x, F0, z, (x + z) % 2 == 0 ? "polished_andesite" : "tuff_bricks");
        }
      }
    }
    t.set(56, SEAT, 24, BlockState.of("smoker", "facing", "west", "lit", false));
    t.set(56, SEAT, 25, BlockState.of("furnace", "facing", "west", "lit", false));
    t.set(55, SEAT, 18, BlockState.of("water_cauldron", "level", 3));
    t.set(51, SEAT, 22, "crafting_table");
    t.set(52, SEAT, 22, Parts.log("stripped_spruce_log", "x"));
    Furnishing.table(t, 51, SEAT, 23);
    Furnishing.table(t, 52, SEAT, 23);
    Furnishing.candles(t, 52, SEAT + 1, 23, 1, false);
    for (final int x : new int[] {54, 55, 56}) {
      t.set(x, SEAT, 28, BlockState.of("barrel", "facing", "up", "open", false));
    }
    Furnishing.chest(t, 49, SEAT, 28, "north");
    t.set(51, CEILING - 1, ManorShell.beamLines(room).get(1), Parts.lantern(true));
    t.set(56, CEILING, 28, "cobweb");
  }
}
