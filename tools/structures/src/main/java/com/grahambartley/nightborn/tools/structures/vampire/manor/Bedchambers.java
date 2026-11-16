package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.F1;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.TOP;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Facing;
import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Furnishing;

public final class Bedchambers {

  public static final int SEAT = F1 + 1;
  public static final int TESTER = TOP - 1;
  public static final int STUDY = TOP + 1;

  private Bedchambers() {}

  public static void build(final Template t, final Turret turret) {
    master(t);
    westChamber(t);
    eastChamber(t, turret);
    sittingRoom(t);
    study(t);
  }

  public static void fourPoster(
      final Template t, final String coffin, final int x, final int z, final String head) {
    Furnishing.coffin(t, coffin, x, SEAT, z, head);
    final Pos step = Facing.STEP.get(head);
    final Pos side = Facing.STEP.get(Facing.CLOCKWISE.get(head));
    final int x0 = Math.min(x - step.x(), x + 2 * step.x()) - Math.abs(side.x());
    final int x1 = Math.max(x - step.x(), x + 2 * step.x()) + Math.abs(side.x());
    final int z0 = Math.min(z - step.z(), z + 2 * step.z()) - Math.abs(side.z());
    final int z1 = Math.max(z - step.z(), z + 2 * step.z()) + Math.abs(side.z());
    for (int px = x0; px <= x1; px++) {
      for (int pz = z0; pz <= z1; pz++) {
        t.set(px, TESTER, pz, Parts.slab("dark_oak_slab", "top"));
      }
    }
    for (final int px : new int[] {x0, x1}) {
      for (final int pz : new int[] {z0, z1}) {
        for (int y = SEAT; y < TESTER; y++) {
          t.set(px, y, pz, StairHall.fence(false, false));
        }
      }
    }
    final int hx = x + 2 * step.x();
    final int hz = z + 2 * step.z();
    Furnishing.drape(t, Manor.BURGUNDY, hx, SEAT, hz, TESTER - SEAT, Facing.OPPOSITE.get(head));
  }

  private static void master(final Template t) {
    fourPoster(t, Manor.DARK_OAK_COFFIN, 19, 21, "east");
    fourPoster(t, Manor.DARK_OAK_COFFIN, 19, 24, "east");
    Furnishing.chair(t, 15, SEAT, 20, "west");
    Furnishing.chair(t, 15, SEAT, 22, "west");
    Furnishing.candelabra(t, 16, SEAT, 21);
    Furnishing.table(t, 14, SEAT, 25);
    Furnishing.candles(t, 14, SEAT + 1, 25, 2, false);
    Furnishing.chair(t, 15, SEAT, 25, "west");
    Furnishing.chest(t, 14, SEAT, 26, "east");
    for (final int z : new int[] {19, 25}) {
      Furnishing.drape(t, Manor.BURGUNDY, 13, SEAT, z, 4, "east");
    }
    for (final int x : new int[] {16, 18}) {
      Furnishing.drape(t, Manor.BURGUNDY, x, SEAT, 26, 4, "north");
    }
    t.set(14, SEAT + 1, 18, StairHall.portrait("south"));
  }

  private static void westChamber(final Template t) {
    fourPoster(t, Manor.SPRUCE_COFFIN, 15, 13, "west");
    Furnishing.table(t, 19, SEAT, 13);
    Furnishing.candelabra(t, 19, SEAT + 1, 13);
    Furnishing.chair(t, 18, SEAT, 13, "east");
    for (int y = SEAT; y <= SEAT + 2; y++) {
      t.set(21, y, 10, "bookshelf");
      t.set(21, y, 11, "bookshelf");
    }
    t.set(21, SEAT, 14, BlockState.of("water_cauldron", "level", 1));
    Furnishing.chest(t, 20, SEAT, 16, "north");
    for (final int z : new int[] {10, 15}) {
      Furnishing.drape(t, Manor.EMERALD, 13, SEAT, z, 4, "east");
    }
    for (final int x : new int[] {15, 19}) {
      Furnishing.drape(t, Manor.EMERALD, x, SEAT, 9, 4, "south");
    }
  }

  private static void eastChamber(final Template t, final Turret turret) {
    Furnishing.coffin(t, Manor.SPRUCE_COFFIN, 34, SEAT, 20, "north");
    Furnishing.coffin(t, Manor.SPRUCE_COFFIN, 37, SEAT, 20, "north");
    Furnishing.candles(t, 35, SEAT, 19, 3, false);
    Furnishing.chair(t, 38, SEAT, 17, "east");
    Furnishing.candelabra(t, 35, SEAT, 17);
    for (final int x : new int[] {34, 35}) {
      Furnishing.drape(t, Manor.BURGUNDY, x, SEAT, 21, 4, "north");
    }
    Furnishing.chair(t, 41, SEAT, 23, "south");
    t.set(33, SEAT + 1, 20, StairHall.portrait("east"));
    t.set(
        turret.ladderX() + 1,
        SEAT,
        Turret.LADDER_Z,
        BlockState.of("candle", "candles", 2, "lit", false, "waterlogged", false));
  }

  private static void sittingRoom(final Template t) {
    Furnishing.chair(t, 37, SEAT, 11, "east");
    Furnishing.chair(t, 37, SEAT, 13, "east");
    Furnishing.table(t, 35, SEAT, 11);
    Furnishing.table(t, 35, SEAT, 12);
    Furnishing.candelabra(t, 35, SEAT + 1, 12);
    Furnishing.chalice(t, 35, SEAT + 1, 11, "full");
    for (int z = 9; z <= 14; z++) {
      for (int y = SEAT; y <= SEAT + 2; y++) {
        t.set(33, y, z, "bookshelf");
      }
    }
    Furnishing.chest(t, 38, SEAT, 14, "west");
    t.set(36, SEAT + 2, 9, StairHall.portrait("south"));
    for (final int x : new int[] {34, 38}) {
      Furnishing.drape(t, Manor.EMERALD, x, SEAT, 9, 4, "south");
    }
  }

  private static void study(final Template t) {
    final int x = Turret.CX;
    final int z = Turret.CZ;
    Furnishing.table(t, x + 1, STUDY, z);
    Furnishing.candelabra(t, x + 1, STUDY + 1, z);
    Furnishing.chair(t, x, STUDY, z, "east");
    Furnishing.lectern(t, x, STUDY, z - 2, "south");
    Furnishing.chest(t, x + 2, STUDY, z, "west");
    t.set(x + 1, STUDY + 4, z - 1, "cobweb");
  }
}
