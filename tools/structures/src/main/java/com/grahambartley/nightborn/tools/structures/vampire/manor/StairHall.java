package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.F0;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.F1;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.HALL_E;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.MAIN_N;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.MAIN_S;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.MID_X;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.TOP;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.WING_E;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Furnishing;
import com.grahambartley.nightborn.tools.structures.vampire.RouteRoom;

public final class StairHall {

  public static final int WEST = WING_E + 1;
  public static final int EAST = HALL_E - 1;
  public static final int LANDING = F0 + 1 + RouteRoom.HOUSEHOLD;
  public static final int LANDING_FRONT = MAIN_N + 7;
  public static final int LANDING_BACK = LANDING_FRONT - (F1 - LANDING);
  public static final int GALLERY_FRONT = MAIN_S - 2;
  public static final int FLIGHT_FOOT = LANDING_FRONT + LANDING - F0;
  public static final String STEP = "dark_oak_stairs";

  private StairHall() {}

  public static void build(final Template t) {
    parquet(t);
    void_(t);
    landing(t);
    flights(t);
    galleries(t);
    balustrade(t);
    furnish(t);
  }

  private static void parquet(final Template t) {
    for (int x = WEST; x <= EAST; x++) {
      for (int z = MAIN_N + 1; z < MAIN_S; z++) {
        t.set(x, F0, z, Math.floorMod(x + z, 4) < 2 ? "dark_oak_planks" : "spruce_planks");
      }
    }
  }

  private static void void_(final Template t) {
    for (int x = WEST; x <= EAST; x++) {
      for (int z = MAIN_N + 1; z < MAIN_S; z++) {
        t.set(x, F1, z, BlockState.AIR);
      }
    }
  }

  private static void landing(final Template t) {
    for (int x = WEST; x <= EAST; x++) {
      for (int z = MAIN_N + 1; z <= LANDING_BACK; z++) {
        t.set(x, LANDING, z, Manor.PLANKS);
      }
    }
    for (int x = MID_X - 1; x <= MID_X + 1; x++) {
      for (int z = LANDING_BACK + 1; z <= LANDING_FRONT; z++) {
        t.set(x, LANDING, z, Manor.PLANKS);
      }
    }
  }

  private static void flights(final Template t) {
    for (int x = MID_X - 1; x <= MID_X + 1; x++) {
      Parts.flight(
          t, STEP, Manor.PLANKS, x, LANDING_FRONT + 1, "north", LANDING, LANDING - F0, F0 + 1);
    }
    for (final int x0 : new int[] {WEST, MID_X + 2}) {
      for (int x = x0; x < x0 + 3; x++) {
        Parts.flight(t, STEP, Manor.PLANKS, x, LANDING_FRONT, "south", F1, F1 - LANDING, LANDING);
      }
    }
  }

  private static void galleries(final Template t) {
    for (int z = LANDING_FRONT + 1; z < MAIN_S; z++) {
      for (int x = WEST; x <= EAST; x++) {
        final boolean side = x < MID_X - 1 || x > MID_X + 1;
        if (side || z >= GALLERY_FRONT) {
          t.set(x, F1, z, Manor.PLANKS);
        }
      }
    }
  }

  private static void balustrade(final Template t) {
    for (int z = LANDING_FRONT + 1; z < GALLERY_FRONT; z++) {
      t.set(MID_X - 2, F1 + 1, z, Parts.fence());
      t.set(MID_X + 2, F1 + 1, z, Parts.fence());
    }
    for (int x = MID_X - 1; x <= MID_X + 1; x++) {
      t.set(x, F1 + 1, GALLERY_FRONT, Parts.fence());
    }
    t.set(MID_X - 2, F1 + 1, GALLERY_FRONT, Parts.fence());
    t.set(MID_X + 2, F1 + 1, GALLERY_FRONT, Parts.fence());
    for (final int x : new int[] {MID_X - 2, MID_X + 2}) {
      for (int z = LANDING_BACK + 1; z <= LANDING_FRONT; z++) {
        final int tread = LANDING + 1 + z - (LANDING_BACK + 1);
        t.set(x, tread + 1, z, Parts.fence());
      }
      for (int z = LANDING_FRONT + 1; z < FLIGHT_FOOT; z++) {
        final int tread = LANDING - (z - LANDING_FRONT - 1);
        for (int y = F0 + 1; y <= tread + 1; y++) {
          t.set(x, y, z, Parts.fence());
        }
      }
      t.set(x, F0 + 1, FLIGHT_FOOT, Parts.log("dark_oak_log", "y"));
      t.set(x, F0 + 2, FLIGHT_FOOT, Parts.lantern(false));
    }
  }

  private static void furnish(final Template t) {
    Furnishing.chandelier(t, MID_X, TOP - 1, FLIGHT_FOOT);
    Furnishing.chandelier(t, MID_X, TOP - 1, MAIN_N + 2);
    for (int x = MID_X - 2; x <= MID_X + 2; x++) {
      for (int z = FLIGHT_FOOT + 1; z < MAIN_S; z++) {
        final boolean border = x == MID_X - 2 || x == MID_X + 2 || z == MAIN_S - 1;
        t.set(x, F0 + 1, z, border ? "black_carpet" : "red_carpet");
      }
    }
    Furnishing.chair(t, WEST + 1, F0 + 1, MAIN_S - 1, "east");
    Furnishing.table(t, EAST - 1, F0 + 1, MAIN_S - 1);
    Furnishing.candelabra(t, EAST - 1, F0 + 2, MAIN_S - 1);
    Furnishing.chair(t, EAST, F0 + 1, LANDING_FRONT + 2, "west");
    Furnishing.chair(t, WEST, F0 + 1, LANDING_FRONT + 2, "east");
    for (final int z : new int[] {LANDING_FRONT + 1, MAIN_S - 2}) {
      Furnishing.portrait(t, EAST, F0 + 3, z, "west");
      Furnishing.portrait(t, WEST, F1 + 2, z, "east");
      Furnishing.portrait(t, EAST, F1 + 2, z, "west");
    }
    for (final int z : new int[] {LANDING_FRONT + 1, MAIN_S - 3}) {
      Furnishing.portrait(t, WEST, F0 + 3, z, "east");
    }
    Furnishing.candelabra(t, WEST, LANDING + 1, MAIN_N + 1);
    Furnishing.candelabra(t, EAST, LANDING + 1, MAIN_N + 1);
    Furnishing.drape(t, Manor.BURGUNDY, MID_X - 2, LANDING + 2, MAIN_N + 1, 4, "south");
    Furnishing.drape(t, Manor.BURGUNDY, MID_X + 2, LANDING + 2, MAIN_N + 1, 4, "south");
  }
}
