package com.grahambartley.nightborn.tools.structures.vampire.mountain;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.manor.Parts;

public final class Wards {

  public static final int FOUNTAIN_Z = 13;
  public static final int[][] GRAVES = {
    {45, 22}, {45, 26}, {45, 30}, {45, 34}, {50, 22}, {50, 26}, {50, 30}, {50, 34}
  };
  public static final int[][] UPPER_LAMPS = {{25, 12}, {37, 12}, {27, 41}, {35, 41}};
  public static final int[][] LOWER_LAMPS = {{15, 48}, {47, 48}, {26, 52}, {36, 52}};

  private Wards() {}

  public static void build(final Template t, final MountainCastle.Layout layout) {
    final int mid = MountainCastle.MID;
    path(t, MountainCastle.LOW, Ramparts.STAIR_FOOT + 1, Barbican.NORTH - 1);
    path(t, MountainCastle.UP, Donjon.SOUTH + 1, Crag.UPPER_SOUTH - Ramparts.THICK);
    path(t, MountainCastle.UP, FOUNTAIN_Z + 2, Donjon.NORTH - 1);
    for (final int[] lamp : UPPER_LAMPS) {
      lamp(t, lamp[0], MountainCastle.UP + 1, lamp[1]);
    }
    for (final int[] lamp : LOWER_LAMPS) {
      lamp(t, lamp[0], MountainCastle.LOW + 1, lamp[1]);
    }
    t.set(mid, MountainCastle.UP + 1, FOUNTAIN_Z, "nightborn:blood_fountain");
    for (final int[] side : new int[][] {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}) {
      t.set(mid + side[0], MountainCastle.UP, FOUNTAIN_Z + side[1], "polished_blackstone_bricks");
    }
    for (final int dx : new int[] {-3, 3}) {
      t.set(mid + dx, MountainCastle.UP + 1, Donjon.SOUTH + 1, "chiseled_stone_bricks");
      t.set(
          mid + dx,
          MountainCastle.UP + 2,
          Donjon.SOUTH + 1,
          BlockState.of(MountainCastle.GARGOYLE, "facing", "south"));
    }
    if (!layout.chapel()) {
      graveyard(t);
    }
  }

  private static void path(final Template t, final int level, final int from, final int to) {
    for (int z = from; z <= to; z++) {
      for (int x = MountainCastle.MID - 1; x <= MountainCastle.MID + 1; x++) {
        t.set(x, level, z, MountainCastle.FOOT.at(x, level, z));
      }
    }
  }

  private static void lamp(final Template t, final int x, final int y, final int z) {
    t.set(x, y, z, BlockState.of("stone_brick_wall", "waterlogged", false));
    t.set(x, y + 1, z, Parts.fence());
    t.set(x, y + 2, z, Parts.lantern(false));
  }

  private static void graveyard(final Template t) {
    final int y = MountainCastle.UP + 1;
    for (final int[] grave : GRAVES) {
      t.set(grave[0], y, grave[1], BlockState.of("cobblestone_wall", "waterlogged", false));
      t.set(grave[0], y + 1, grave[1], BlockState.of("skeleton_skull", "rotation", 4));
      t.set(grave[0] + 1, MountainCastle.UP, grave[1], "coarse_dirt");
      t.set(grave[0] + 2, MountainCastle.UP, grave[1], "coarse_dirt");
    }
    for (int z = Chapel.NORTH; z <= Chapel.SOUTH; z++) {
      if (z != Chapel.DOOR_Z) {
        t.set(Chapel.WEST, y, z, "iron_bars");
      }
    }
    t.set(Chapel.MID_X, y, Chapel.NORTH + 2, "nightborn:carved_gothic_stone");
    t.set(
        Chapel.MID_X,
        y + 1,
        Chapel.NORTH + 2,
        BlockState.of(MountainCastle.GARGOYLE, "facing", "south"));
  }
}
