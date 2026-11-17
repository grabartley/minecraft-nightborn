package com.grahambartley.nightborn.tools.structures.vampire.mountain;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Furnishing;
import com.grahambartley.nightborn.tools.structures.vampire.manor.Parts;

public final class DonjonRooms {

  public static final int[] BEDS_X = {
    MountainCastle.MID - 5, MountainCastle.MID - 2, MountainCastle.MID + 2, MountainCastle.MID + 5
  };
  public static final int[] UPRIGHT_X = {
    MountainCastle.MID - 3, MountainCastle.MID, MountainCastle.MID + 6
  };
  public static final int[] DORMITORIES = {1, 2, 3};
  public static final int STUDY = 4;
  public static final int ANTECHAMBER = 5;
  public static final int FEWER_UPRIGHT = 2;
  public static final int DAIS_SOUTH = Donjon.NORTH + 5;
  public static final int ELDEST_COFFIN_Z = Donjon.NORTH + 4;

  private DonjonRooms() {}

  public static void build(final Template t) {
    hall(t);
    for (final int storey : DORMITORIES) {
      dormitory(t, storey);
    }
    study(t);
    antechamber(t);
    eyrie(t);
  }

  static String coffin(final int storey, final int index) {
    return (storey + index) % 2 == 0
        ? MountainCastle.GOTHIC_COFFIN
        : MountainCastle.DARK_OAK_COFFIN;
  }

  static int upright(final int storey) {
    return storey == DORMITORIES[DORMITORIES.length - 1] ? FEWER_UPRIGHT : UPRIGHT_X.length;
  }

  private static void hall(final Template t) {
    final int y = Donjon.GROUND + 1;
    final int mid = MountainCastle.MID;
    for (int z = Donjon.NORTH + 2; z < Donjon.SOUTH; z++) {
      t.set(mid, y, z, "red_carpet");
    }
    for (int z = Donjon.NORTH + 4; z <= Donjon.NORTH + 10; z++) {
      Furnishing.table(t, mid + 4, y, z);
      if (z % 2 == 0) {
        Furnishing.chair(t, mid + 3, y, z, "east");
        Furnishing.chair(t, mid + 5, y, z, "west");
      }
    }
    Furnishing.chalice(t, mid + 4, y + 1, Donjon.NORTH + 5, "villager");
    Furnishing.candles(t, mid + 4, y + 1, Donjon.NORTH + 8, 3, false);
    Furnishing.chalice(t, mid + 4, y + 1, Donjon.NORTH + 10, "player");
    for (final int z : new int[] {Donjon.NORTH + 4, Donjon.SOUTH - 4}) {
      Furnishing.chandelier(t, mid, Donjon.floor(1) - 1, z);
    }
    for (final int dx : new int[] {-2, 2}) {
      Furnishing.candelabra(t, mid + dx, y, Donjon.NORTH + 1);
      Furnishing.drape(t, MountainCastle.BURGUNDY, mid + dx * 2, y, Donjon.NORTH + 1, 4, "south");
    }
    Furnishing.portrait(t, mid, y + 2, Donjon.NORTH + 1, "south");
    for (final int z : new int[] {Donjon.NORTH + 3, Donjon.NORTH + 9}) {
      t.set(Donjon.EAST - 1, y + 2, z, BlockState.of("nightborn:trophy_mount", "facing", "west"));
    }
    t.set(
        Donjon.WEST + 1,
        y,
        Donjon.NORTH + 3,
        BlockState.of("nightborn:weapon_rack", "facing", "east"));
    Furnishing.chest(t, Donjon.EAST - 1, y, Donjon.SOUTH - 1, "west", MountainCastle.LOOT);
  }

  private static void dormitory(final Template t, final int storey) {
    final int y = Donjon.floor(storey) + 1;
    final int mid = MountainCastle.MID;
    for (int i = 0; i < BEDS_X.length; i++) {
      Furnishing.coffin(t, coffin(storey, i), BEDS_X[i], y, Donjon.NORTH + 2, "north");
    }
    for (int i = 0; i < upright(storey); i++) {
      Furnishing.standingCoffin(
          t, coffin(storey, i + 1), UPRIGHT_X[i], y, Donjon.SOUTH - 1, "south");
    }
    Furnishing.chandelier(t, mid, Donjon.floor(storey + 1) - 1, mid);
    Furnishing.table(t, mid, y, mid + 2);
    Furnishing.chair(t, mid - 1, y, mid + 2, "east");
    Furnishing.chair(t, mid + 1, y, mid + 2, "west");
    Furnishing.candles(t, mid, y + 1, mid + 2, 2 + storey % 2, false);
    final String drape = storey % 2 == 0 ? MountainCastle.EMERALD : MountainCastle.BURGUNDY;
    Furnishing.drape(t, drape, mid, y, Donjon.NORTH + 1, 3, "south");
    Furnishing.portrait(t, Donjon.EAST - 1, y + 1, Donjon.NORTH + 4, "west");
    Furnishing.portrait(t, Donjon.WEST + 1, y + 1, Donjon.NORTH + 4, "east");
    if (upright(storey) == FEWER_UPRIGHT) {
      for (int dy = 0; dy < 3; dy++) {
        t.set(mid + 5, y + dy, Donjon.SOUTH - 1, "bookshelf");
        t.set(mid + 6, y + dy, Donjon.SOUTH - 1, "bookshelf");
      }
      Furnishing.lectern(t, mid + 5, y, Donjon.SOUTH - 3, "north");
      Furnishing.chest(t, mid + 3, y, Donjon.SOUTH - 1, "north", MountainCastle.LOOT);
    }
    t.set(Donjon.WEST + 1, y, Donjon.NORTH + 1, "cobweb");
  }

  private static void study(final Template t) {
    final int y = Donjon.floor(STUDY) + 1;
    final int mid = MountainCastle.MID;
    for (int x = Donjon.WEST + 3; x <= Donjon.EAST - 3; x++) {
      if (Math.abs(x - mid) == 4) {
        continue;
      }
      for (int dy = 0; dy < 4; dy++) {
        t.set(x, y + dy, Donjon.NORTH + 1, "bookshelf");
      }
    }
    for (final int dx : new int[] {-3, 3}) {
      Furnishing.lectern(t, mid + dx, y, Donjon.NORTH + 4, "south");
      Furnishing.table(t, mid + dx, y, mid + 3);
      Furnishing.chair(t, mid + dx, y, mid + 4, "north");
      Furnishing.candelabra(t, mid + dx, y + 1, mid + 3);
    }
    Furnishing.chandelier(t, mid, Donjon.floor(STUDY + 1) - 1, mid);
    Furnishing.drape(t, MountainCastle.EMERALD, mid - 2, y, Donjon.SOUTH - 1, 3, "north");
    Furnishing.drape(t, MountainCastle.EMERALD, mid + 2, y, Donjon.SOUTH - 1, 3, "north");
    Furnishing.portrait(t, mid, y + 1, Donjon.SOUTH - 1, "north");
    Furnishing.chest(t, Donjon.WEST + 1, y, Donjon.NORTH + 1, "east", MountainCastle.LOOT);
    t.set(Donjon.EAST - 1, y + 3, Donjon.NORTH + 1, "cobweb");
  }

  private static void antechamber(final Template t) {
    final int y = Donjon.floor(ANTECHAMBER) + 1;
    final int mid = MountainCastle.MID;
    for (final int dx : new int[] {-5, -2, 2, 5}) {
      t.set(
          mid + dx, y, Donjon.NORTH + 1, BlockState.of("nightborn:weapon_rack", "facing", "south"));
      t.set(
          mid + dx,
          y + 2,
          Donjon.SOUTH - 1,
          BlockState.of("nightborn:trophy_mount", "facing", "north"));
    }
    Furnishing.drape(t, MountainCastle.BURGUNDY, mid, y, Donjon.NORTH + 1, 4, "south");
    for (final int dx : new int[] {-3, 3}) {
      Furnishing.candelabra(t, mid + dx, y, mid);
      t.set(mid + dx, y, mid - 3, "nightborn:carved_gothic_stone");
      Furnishing.chalice(t, mid + dx, y + 1, mid - 3, "player");
    }
    for (int z = Donjon.NORTH + 3; z < Donjon.SOUTH - 1; z++) {
      t.set(mid, y, z, "red_carpet");
    }
    Furnishing.chandelier(t, mid, Donjon.floor(ANTECHAMBER + 1) - 1, mid);
    Furnishing.chest(t, Donjon.WEST + 1, y, Donjon.SOUTH - 1, "east", MountainCastle.LOOT);
    Furnishing.chest(t, Donjon.EAST - 1, y, Donjon.SOUTH - 1, "west", MountainCastle.LOOT);
    t.set(Donjon.WEST + 2, y, Donjon.NORTH + 1, "cobweb");
  }

  private static void eyrie(final Template t) {
    final int y = Donjon.EYRIE + 1;
    final int mid = MountainCastle.MID;
    t.fill(mid - 3, y, Donjon.NORTH + 1, mid + 3, y, DAIS_SOUTH, "polished_blackstone_bricks");
    for (int x = mid - 3; x <= mid + 3; x++) {
      t.set(x, y, DAIS_SOUTH + 1, Parts.stair("polished_blackstone_brick_stairs", "north"));
    }
    Furnishing.coffin(t, MountainCastle.GOTHIC_COFFIN, mid, y + 1, ELDEST_COFFIN_Z, "north");
    for (final int dx : new int[] {-2, 2}) {
      Furnishing.candelabra(t, mid + dx, y + 1, Donjon.NORTH + 2);
      Furnishing.drape(
          t, MountainCastle.BURGUNDY, mid + dx + dx / 2, y + 1, Donjon.NORTH + 1, 4, "south");
    }
    Furnishing.portrait(t, mid, y + 3, Donjon.NORTH + 1, "south");
    Furnishing.oldBloodFont(t, mid + 2, y + 1, DAIS_SOUTH);
    Furnishing.portrait(t, Donjon.WEST + 1, y + 1, Donjon.NORTH + 6, "east");
    Furnishing.portrait(t, Donjon.EAST - 1, y + 1, Donjon.NORTH + 6, "west");
    Furnishing.chest(t, Donjon.WEST + 1, y, Donjon.NORTH + 1, "east", MountainCastle.LOOT);
    Furnishing.chest(t, Donjon.EAST - 1, y, Donjon.NORTH + 1, "west", MountainCastle.LOOT);
    Furnishing.chandelier(t, mid, Donjon.TERRACE - 1, mid);
    for (int dy = 0; dy < 3; dy++) {
      for (int z = Donjon.SOUTH - 4; z <= Donjon.SOUTH - 1; z++) {
        t.set(Donjon.WEST + 1, y + dy, z, "bookshelf");
      }
    }
    Furnishing.lectern(t, Donjon.WEST + 3, y, Donjon.SOUTH - 2, "west");
    Furnishing.table(t, mid - 3, y, Donjon.SOUTH - 2);
    Furnishing.chair(t, mid - 3, y, Donjon.SOUTH - 3, "south");
    Furnishing.chalice(t, mid - 3, y + 1, Donjon.SOUTH - 2, "player");
    t.set(Donjon.EAST - 2, y, Donjon.SOUTH - 1, "cobweb");
  }
}
