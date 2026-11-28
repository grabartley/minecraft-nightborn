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
  public static final int STUDY = Turret.STUDY + 1;

  private Bedchambers() {}

  public static void build(final Template t) {
    westChamber(t);
    master(t);
    sittingRoom(t);
    eastChamber(t);
    study(t);
  }

  public static void halfTester(
      final Template t, final String coffin, final int x, final int z, final String head) {
    Furnishing.coffin(t, coffin, x, SEAT, z, head);
    final Pos step = Facing.STEP.get(head);
    final Pos side = Facing.STEP.get(Facing.CLOCKWISE.get(head));
    final int hx = x + 2 * step.x();
    final int hz = z + 2 * step.z();
    for (int along = 1; along <= 2; along++) {
      for (int across = -1; across <= 1; across++) {
        t.set(
            x + along * step.x() + across * side.x(),
            TESTER,
            z + along * step.z() + across * side.z(),
            Parts.slab("dark_oak_slab", "top"));
      }
    }
    for (final int across : new int[] {-1, 1}) {
      for (int y = SEAT; y < TESTER; y++) {
        t.set(hx + across * side.x(), y, hz + across * side.z(), Parts.fence());
      }
    }
    Furnishing.drape(t, Manor.BURGUNDY, hx, SEAT, hz, TESTER - SEAT, Facing.OPPOSITE.get(head));
  }

  private static void westChamber(final Template t) {
    halfTester(t, Manor.SPRUCE_COFFIN, 22, 24, "west");
    Furnishing.table(t, 28, SEAT, 27);
    Furnishing.candelabra(t, 28, SEAT + 1, 27);
    Furnishing.chair(t, 27, SEAT, 27, "east");
    for (int y = SEAT; y <= SEAT + 2; y++) {
      t.set(30, y, 19, "bookshelf");
      t.set(30, y, 20, "bookshelf");
    }
    t.set(21, SEAT, 28, BlockState.of("water_cauldron", "level", 1));
    Furnishing.chest(t, 21, SEAT, 19, "east");
    Furnishing.portrait(t, 28, SEAT + 2, 28, "north");
  }

  private static void master(final Template t) {
    halfTester(t, Manor.DARK_OAK_COFFIN, 22, 32, "north");
    halfTester(t, Manor.DARK_OAK_COFFIN, 28, 32, "north");
    Furnishing.chair(t, 22, SEAT, 34, "west");
    Furnishing.chair(t, 22, SEAT, 36, "west");
    Furnishing.candelabra(t, 22, SEAT, 38);
    Furnishing.table(t, 26, SEAT, 38);
    Furnishing.candles(t, 26, SEAT + 1, 38, 2, false);
    Furnishing.chair(t, 26, SEAT, 37, "south");
    Furnishing.chest(t, 29, SEAT, 40, "north");
    Furnishing.portrait(t, 25, SEAT + 2, 40, "north");
  }

  private static void sittingRoom(final Template t) {
    Furnishing.chair(t, 54, SEAT, 21, "east");
    Furnishing.chair(t, 54, SEAT, 23, "east");
    Furnishing.table(t, 53, SEAT, 22);
    Furnishing.chalice(t, 53, SEAT + 1, 22, "full");
    for (int x = 46; x <= 48; x++) {
      for (int y = SEAT; y <= SEAT + 2; y++) {
        t.set(x, y, 28, "bookshelf");
      }
    }
    Furnishing.chest(t, 56, SEAT, 28, "west");
    Furnishing.portrait(t, 51, SEAT + 2, 18, "south");
    Furnishing.candelabra(t, 56, SEAT, 18);
  }

  private static void eastChamber(final Template t) {
    halfTester(t, Manor.SPRUCE_COFFIN, 48, 32, "north");
    halfTester(t, Manor.SPRUCE_COFFIN, 54, 32, "north");
    Furnishing.chair(t, 54, SEAT, 36, "east");
    Furnishing.candles(t, 54, SEAT, 37, 3, false);
    Furnishing.chest(t, 47, SEAT, 40, "north");
    Furnishing.portrait(t, 50, SEAT + 2, 40, "north");
  }

  private static void study(final Template t) {
    final int x = Turret.CX;
    final int z = Turret.CZ;
    Furnishing.lectern(t, x - 2, STUDY, z, "east");
    Furnishing.table(t, x - 1, STUDY, z + 2);
    Furnishing.candelabra(t, x - 1, STUDY + 1, z + 2);
    Furnishing.chair(t, x - 2, STUDY, z + 2, "east");
    Furnishing.candelabra(t, x - 3, STUDY, z);
    Furnishing.chest(t, x - 2, STUDY, z - 2, "east");
    t.set(x + 2, STUDY + 6, z - 1, "cobweb");
  }
}
