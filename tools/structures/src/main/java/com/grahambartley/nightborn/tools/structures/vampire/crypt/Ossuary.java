package com.grahambartley.nightborn.tools.structures.vampire.crypt;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Axis;
import com.grahambartley.nightborn.tools.structures.vampire.Furnishing;
import com.grahambartley.nightborn.tools.structures.vampire.Openings;

public final class Ossuary {

  public static final String PATH = "vampire/crypt/ossuary";
  static final int NORTH = 6;
  static final int SOUTH = 8;
  static final int SKULLS_NORTH = 0;
  static final int SKULLS_SOUTH = 8;

  private Ossuary() {}

  public static Template build() {
    final Template t = CryptStair.shell(PATH);
    gallery(t);
    t.fill(5, 1, 2, 7, 3, 3, "air");
    Openings.arch(t, Axis.X, 6, 8, 4, 1, 3, CryptStair.STEP);
    Openings.arch(t, Axis.X, 6, 8, 5, 1, 3, CryptStair.STEP);
    northChapel(t);
    southChapel(t);
    charnel(t, 9, 11, 5, SKULLS_NORTH);
    charnel(t, 1, 4, 9, SKULLS_SOUTH);
    charnel(t, 10, 11, 9, SKULLS_SOUTH);
    altar(t);
    Furnishing.standingCoffin(t, Undercroft.GOTHIC_COFFIN, 11, 1, 7, "east");
    return t;
  }

  private static void gallery(final Template t) {
    t.fill(1, 1, NORTH, 11, 3, SOUTH, "air");
    for (int x = 1; x <= 11; x++) {
      t.set(x, 4, NORTH, vault("north"));
      t.set(x, 4, 7, BlockState.AIR);
      t.set(x, 4, SOUTH, vault("south"));
    }
    for (final int x : new int[] {4, 8}) {
      t.set(x, 4, 7, CryptStair.CAPITAL);
    }
    Furnishing.chandelier(t, 2, 4, 7);
    Furnishing.chandelier(t, 6, 4, 7);
    Furnishing.chandelier(t, 10, 4, 7);
  }

  private static void northChapel(final Template t) {
    t.fill(1, 1, 2, 3, 3, 4, "air");
    Openings.arch(t, Axis.X, 1, 3, 5, 1, 3, CryptStair.STEP);
    Furnishing.coffin(t, Undercroft.STONE_COFFIN, 1, 1, 3, "east");
    Furnishing.chest(t, 3, 1, 2, "south");
    Furnishing.candelabra(t, 1, 1, 2);
    Furnishing.candelabra(t, 2, 1, 2);
    Furnishing.candles(t, 3, 1, 4, 2, false);
    t.set(1, 3, 2, "cobweb");
  }

  private static void southChapel(final Template t) {
    t.fill(5, 1, 10, 9, 3, 12, "air");
    Openings.arch(t, Axis.X, 6, 8, 9, 1, 3, CryptStair.STEP);
    Furnishing.coffin(t, Undercroft.GOTHIC_COFFIN, 5, 1, 10, "east");
    Furnishing.coffin(t, Undercroft.STONE_COFFIN, 8, 1, 10, "east");
    Furnishing.chair(t, 7, 1, 12, "north");
    Furnishing.candelabra(t, 5, 1, 12);
    Furnishing.candelabra(t, 9, 1, 12);
    t.set(9, 3, 12, "cobweb");
  }

  private static void charnel(
      final Template t, final int from, final int to, final int z, final int rotation) {
    for (int x = from; x <= to; x++) {
      t.set(x, 1, z, BlockState.of("bone_block", "axis", "x"));
      t.set(x, 2, z, BlockState.of("skeleton_skull", "rotation", rotation));
      t.set(x, 3, z, BlockState.of("bone_block", "axis", "x"));
    }
  }

  private static void altar(final Template t) {
    t.set(1, 1, 7, "nightborn:carved_gothic_stone");
    Furnishing.chalice(t, 1, 2, 7, "player");
    Furnishing.candelabra(t, 1, 1, NORTH);
    Furnishing.candelabra(t, 1, 1, SOUTH);
    Furnishing.gramophone(t, 2, 1, SOUTH, "north");
    Furnishing.oldBloodFont(t, 2, 1, NORTH);
  }

  private static String vault(final String facing) {
    return BlockState.of(CryptStair.RIB, "facing", facing, "half", "top", "shape", "straight");
  }
}
