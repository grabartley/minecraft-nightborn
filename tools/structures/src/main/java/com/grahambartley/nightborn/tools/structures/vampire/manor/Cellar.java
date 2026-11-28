package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.F0;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.HOUSE_E;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.HOUSE_N;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Furnishing;
import com.grahambartley.nightborn.tools.structures.vampire.Stone;

public final class Cellar {

  public static final ManorShell.Box ROOM = ManorShell.KITCHEN;
  public static final int FLOOR = 0;
  public static final int STAND = FLOOR + 1;
  public static final int STAIR_X = ROOM.x0();
  public static final int STAIR_TOP_Z = 24;
  public static final int STAIR_FOOT_Z = STAIR_TOP_Z - F0 + 1;
  public static final int FONT_X = 52;
  public static final int FONT_Z = ROOM.z0() + 1;
  static final Stone FLAGS =
      new Stone("deepslate_tiles", "cracked_deepslate_tiles", "polished_deepslate", 40);

  private Cellar() {}

  public static void build(final Template t) {
    for (int x = ROOM.x0(); x <= ROOM.x1(); x++) {
      for (int z = ROOM.z0(); z <= ROOM.z1(); z++) {
        t.set(x, FLOOR, z, FLAGS.at(x, FLOOR, z));
        for (int y = STAND; y < F0; y++) {
          t.set(x, y, z, BlockState.AIR);
        }
      }
    }
    stair(t);
    vents(t);
    font(t);
    stores(t);
  }

  private static void stair(final Template t) {
    for (int x = STAIR_X; x <= STAIR_X + 1; x++) {
      Parts.flight(t, "dark_oak_stairs", Manor.PLANKS, x, STAIR_TOP_Z, "south", F0, F0, STAND);
      for (int z = STAIR_FOOT_Z; z < STAIR_TOP_Z; z++) {
        t.set(x, F0, z, BlockState.AIR);
      }
      t.set(x, F0 + 1, STAIR_FOOT_Z - 1, Parts.fence());
    }
    for (int z = STAIR_FOOT_Z; z < STAIR_TOP_Z; z++) {
      t.set(STAIR_X + 2, F0 + 1, z, Parts.fence());
    }
  }

  private static void vents(final Template t) {
    for (final int z : new int[] {ROOM.z0() + 7, ROOM.z1()}) {
      for (int y = STAND + 1; y <= STAND + 2; y++) {
        t.set(HOUSE_E, y, z, "iron_bars");
        t.set(HOUSE_E - 1, y, z, "iron_bars");
      }
    }
    for (final int x : new int[] {ROOM.x0() + 3, ROOM.x1() - 2}) {
      for (int y = STAND + 1; y <= STAND + 2; y++) {
        t.set(x, y, HOUSE_N, "iron_bars");
        t.set(x, y, HOUSE_N + 1, "iron_bars");
      }
    }
  }

  private static void font(final Template t) {
    for (int x = FONT_X - 1; x <= FONT_X + 1; x++) {
      for (int z = FONT_Z - 1; z <= FONT_Z + 1; z++) {
        t.set(x, FLOOR, z, "polished_blackstone_bricks");
      }
    }
    Furnishing.oldBloodFont(t, FONT_X, STAND, FONT_Z);
    Furnishing.candles(t, FONT_X - 1, STAND, FONT_Z, 3, true);
    Furnishing.candles(t, FONT_X + 1, STAND, FONT_Z, 2, true);
    Furnishing.chandelier(t, FONT_X, F0 - 1, FONT_Z + 3, true);
    Furnishing.lectern(t, FONT_X - 3, STAND, FONT_Z + 1, "east");
  }

  private static void stores(final Template t) {
    for (int z = ROOM.z0() + 3; z < ROOM.z0() + 7; z++) {
      for (int y = STAND; y <= STAND + 1; y++) {
        t.set(ROOM.x1(), y, z, BlockState.of("barrel", "facing", "west", "open", false));
      }
    }
    for (int x = ROOM.x1() - 4; x <= ROOM.x1() - 1; x++) {
      t.set(x, STAND, ROOM.z1(), BlockState.of("barrel", "facing", "up", "open", false));
    }
    Furnishing.chest(t, ROOM.x1() - 5, STAND, ROOM.z1(), "north");
    t.set(ROOM.x0() + 4, STAND, ROOM.z1(), "cobweb");
    t.set(ROOM.x1(), F0 - 1, ROOM.z0(), "cobweb");
  }
}
