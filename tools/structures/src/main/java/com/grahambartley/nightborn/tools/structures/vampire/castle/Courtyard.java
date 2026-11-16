package com.grahambartley.nightborn.tools.structures.vampire.castle;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Stone;

public final class Courtyard {

  public static final int FOUNTAIN_Z = 33;
  public static final int[] LAMPS_Z = {32, 34};
  public static final int[][] GRAVES = {{6, 16}, {6, 20}, {6, 24}, {9, 18}, {9, 22}};

  static final Stone PATH =
      new Stone("stone_bricks", "mossy_stone_bricks", "cracked_stone_bricks", 35);

  private Courtyard() {}

  public static String ground(final int x, final int z) {
    final int roll = Stone.roll(x, Castle.GROUND, z);
    if (roll < 40) {
      return "coarse_dirt";
    }
    if (roll < 55) {
      return "podzol";
    }
    if (roll < 65) {
      return "gravel";
    }
    return "grass_block";
  }

  public static void build(final Template t, final Castle.Layout layout) {
    for (int x = 2; x < Castle.SIZE - 2; x++) {
      for (int z = 2; z < Castle.SIZE - 2; z++) {
        t.set(x, 0, z, "dirt");
        t.set(x, Castle.GROUND, z, ground(x, z));
      }
    }
    for (int z = Keep.SOUTH + 1; z < Castle.SIZE - 2; z++) {
      for (int x = Castle.MID - 1; x <= Castle.MID + 1; x++) {
        t.set(x, Castle.GROUND, z, PATH.at(x, Castle.GROUND, z));
      }
    }
    for (final int z : LAMPS_Z) {
      lamp(t, Castle.MID - 3, z);
      lamp(t, Castle.MID + 3, z);
    }
    if (layout.fountain()) {
      t.set(Castle.MID, Castle.FLOOR, FOUNTAIN_Z, "nightborn:blood_fountain");
      for (final int[] side : new int[][] {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}) {
        t.set(
            Castle.MID + side[0],
            Castle.GROUND,
            FOUNTAIN_Z + side[1],
            "polished_blackstone_bricks");
      }
    } else {
      for (final int[] grave : GRAVES) {
        grave(t, grave[0], grave[1]);
      }
    }
    for (final int[] bush : new int[][] {{5, 30}, {33, 12}, {35, 27}, {8, 9}, {31, 33}}) {
      t.set(bush[0], Castle.FLOOR, bush[1], "dead_bush");
    }
  }

  private static void lamp(final Template t, final int x, final int z) {
    t.set(x, Castle.FLOOR, z, "polished_blackstone_wall");
    t.set(
        x,
        Castle.FLOOR + 1,
        z,
        BlockState.of(
            "dark_oak_fence",
            "north",
            false,
            "south",
            false,
            "east",
            false,
            "west",
            false,
            "waterlogged",
            false));
    t.set(
        x,
        Castle.FLOOR + 2,
        z,
        BlockState.of("soul_lantern", "hanging", false, "waterlogged", false));
  }

  private static void grave(final Template t, final int x, final int z) {
    t.set(x, Castle.FLOOR, z, "cobblestone_wall");
    t.set(x, Castle.FLOOR + 1, z, BlockState.of("skeleton_skull", "rotation", 4));
    t.set(x + 1, Castle.GROUND, z, "coarse_dirt");
    t.set(x + 2, Castle.GROUND, z, "coarse_dirt");
  }
}
