package com.grahambartley.nightborn.tools.structures.vampire.crypt;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Axis;
import com.grahambartley.nightborn.tools.structures.vampire.Furnishing;
import com.grahambartley.nightborn.tools.structures.vampire.Openings;

public final class Undercroft {

  public static final String PATH = "vampire/crypt/undercroft";
  public static final int[] BAYS_X = {2, 6, 10};
  public static final int[] BAYS_Z = {6, 10};
  public static final int[][] COLUMNS = {{4, 8}, {8, 8}};
  static final String STONE_COFFIN = "nightborn:stone_brick_coffin";
  static final String GOTHIC_COFFIN = "nightborn:gothic_stone_brick_coffin";

  private Undercroft() {}

  public static Template build() {
    final Template t = CryptStair.shell(PATH);
    t.fill(1, 1, 5, 11, 4, 11, "air");
    Openings.arch(t, Axis.X, 5, 7, 4, 1, 3, CryptStair.STEP);
    vault(t);
    columns(t);
    tombs(t);
    furnish(t);
    return t;
  }

  private static void vault(final Template t) {
    CryptStair.MASONRY.fill(t, 1, 4, 5, 11, 4, 11);
    for (int x = 1; x <= 11; x++) {
      t.set(x, 4, 8, CryptStair.CAPITAL);
    }
    for (final int x : new int[] {4, 8}) {
      for (int z = 5; z <= 11; z++) {
        t.set(x, 4, z, CryptStair.CAPITAL);
      }
    }
    for (final int x : BAYS_X) {
      for (final int z : BAYS_Z) {
        t.set(x, 4, z, BlockState.AIR);
        rib(t, x - 1, z, "west");
        rib(t, x + 1, z, "east");
        rib(t, x, z - 1, "north");
        rib(t, x, z + 1, "south");
      }
    }
  }

  private static void rib(final Template t, final int x, final int z, final String facing) {
    t.set(
        x,
        4,
        z,
        BlockState.of(CryptStair.RIB, "facing", facing, "half", "top", "shape", "straight"));
  }

  private static void columns(final Template t) {
    for (final int[] column : COLUMNS) {
      t.set(column[0], 1, column[1], CryptStair.CAPITAL);
      t.set(column[0], 2, column[1], CryptStair.SHAFT);
      t.set(column[0], 3, column[1], CryptStair.CAPITAL);
    }
  }

  private static void tombs(final Template t) {
    final String[] coffins = {STONE_COFFIN, GOTHIC_COFFIN, STONE_COFFIN};
    for (int i = 0; i < BAYS_X.length; i++) {
      final int x = BAYS_X[i];
      Openings.arch(t, Axis.X, x - 1, x + 1, 12, 1, 3, CryptStair.STEP);
      Openings.arch(t, Axis.X, x - 1, x + 1, 13, 1, 3, CryptStair.STEP);
      Furnishing.coffin(t, coffins[i], x, 1, 12, "south");
    }
    Furnishing.standingCoffin(t, GOTHIC_COFFIN, 11, 1, 6, "east");
    Furnishing.standingCoffin(t, GOTHIC_COFFIN, 11, 1, 10, "east");
  }

  private static void furnish(final Template t) {
    t.set(1, 1, 8, "nightborn:carved_gothic_stone");
    Furnishing.chalice(t, 1, 2, 8, "villager");
    Furnishing.candelabra(t, 1, 1, 7);
    Furnishing.candelabra(t, 1, 1, 9);
    Furnishing.table(t, 6, 1, 6);
    Furnishing.chair(t, 5, 1, 6, "east");
    Furnishing.chair(t, 7, 1, 6, "west");
    Furnishing.gramophone(t, 6, 2, 6, "south");
    t.set(
        1, 1, 10, BlockState.of("lectern", "facing", "east", "has_book", false, "powered", false));
    t.set(1, 3, 11, "cobweb");
    Furnishing.chest(t, 1, 1, 5, "east");
    Furnishing.oldBloodFont(t, 1, 1, 6);
    Furnishing.chandelier(t, 6, 4, 6);
    Furnishing.chandelier(t, 6, 4, 10);
    t.set(11, 3, 5, "cobweb");
    t.set(8, 1, 11, BlockState.of("skeleton_skull", "rotation", 8));
    Furnishing.candles(t, 3, 1, 11, 3, false);
    t.set(6, 0, 8, "nightborn:carved_gothic_stone");
  }
}
