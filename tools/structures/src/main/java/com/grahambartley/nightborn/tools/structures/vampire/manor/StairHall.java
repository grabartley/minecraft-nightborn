package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.F0;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.F1;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.HALL_E;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.HALL_W;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.MID_X;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.ROOM_N;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.ROOM_S;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.TOP;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Furnishing;

public final class StairHall {

  public static final int WEST = HALL_W + 1;
  public static final int EAST = HALL_E - 1;
  public static final int FLIGHT = 3;
  public static final int LANDING = F0 + 4;
  public static final int LANDING_BACK = ROOM_N + 1;
  public static final int LANDING_FRONT = LANDING_BACK + F1 - LANDING;
  public static final int FLIGHT_FOOT = LANDING_FRONT + LANDING - F0 + 1;
  public static final int GALLERY_EDGE_W = WEST + FLIGHT;
  public static final int GALLERY_EDGE_E = EAST - FLIGHT;
  public static final int GALLERY_FRONT = ROOM_S - 2;
  public static final String STEP = "dark_oak_stairs";

  private StairHall() {}

  public static boolean overVoid(final int x, final int z) {
    return x > GALLERY_EDGE_W && x < GALLERY_EDGE_E && z > LANDING_FRONT && z < GALLERY_FRONT;
  }

  public static void build(final Template t) {
    floor(t);
    void_(t);
    understair(t);
    landing(t);
    flights(t);
    galleries(t);
    balustrade(t);
    furnish(t);
  }

  private static void floor(final Template t) {
    for (int x = WEST; x <= EAST; x++) {
      for (int z = ROOM_N; z <= ROOM_S; z++) {
        t.set(x, F0, z, (x + z) % 2 == 0 ? "polished_deepslate" : "nightborn:carved_gothic_stone");
      }
    }
  }

  private static void void_(final Template t) {
    for (int x = WEST; x <= EAST; x++) {
      for (int z = ROOM_N; z <= ROOM_S; z++) {
        t.set(x, F1, z, BlockState.AIR);
      }
    }
  }

  private static void understair(final Template t) {
    for (int x = WEST; x <= EAST; x++) {
      for (int z = ROOM_N; z <= LANDING_FRONT; z++) {
        for (int y = F0 + 1; y < LANDING; y++) {
          t.set(x, y, z, ManorShell.finish(x, y, z));
        }
      }
    }
  }

  private static void landing(final Template t) {
    for (int x = WEST; x <= EAST; x++) {
      for (int z = ROOM_N; z <= LANDING_FRONT; z++) {
        final boolean back = z <= LANDING_BACK;
        final boolean middle = x >= GALLERY_EDGE_W && x <= GALLERY_EDGE_E;
        if (back || middle) {
          t.set(x, LANDING, z, Manor.PLANKS);
        }
      }
    }
  }

  private static void flights(final Template t) {
    for (int x = MID_X - 1; x <= MID_X + 1; x++) {
      Parts.flight(
          t, STEP, Manor.PLANKS, x, LANDING_FRONT + 1, "north", LANDING, LANDING - F0, F0 + 1);
    }
    for (final int x0 : new int[] {WEST, EAST - FLIGHT + 1}) {
      for (int x = x0; x < x0 + FLIGHT; x++) {
        Parts.flight(t, STEP, Manor.PLANKS, x, LANDING_FRONT, "south", F1, F1 - LANDING, LANDING);
      }
    }
  }

  private static void galleries(final Template t) {
    for (int x = WEST; x <= EAST; x++) {
      for (int z = LANDING_FRONT + 1; z <= ROOM_S; z++) {
        if (!overVoid(x, z)) {
          t.set(x, F1, z, Manor.PLANKS);
        }
      }
    }
  }

  private static void balustrade(final Template t) {
    for (int z = LANDING_FRONT + 1; z <= GALLERY_FRONT; z++) {
      t.set(GALLERY_EDGE_W, F1 + 1, z, Parts.fence());
      t.set(GALLERY_EDGE_E, F1 + 1, z, Parts.fence());
    }
    for (int x = GALLERY_EDGE_W; x <= GALLERY_EDGE_E; x++) {
      t.set(x, F1 + 1, GALLERY_FRONT, Parts.fence());
    }
    for (int z = LANDING_BACK + 1; z <= LANDING_FRONT; z++) {
      t.set(GALLERY_EDGE_W, LANDING + 1, z, Parts.fence());
      t.set(GALLERY_EDGE_E, LANDING + 1, z, Parts.fence());
    }
    for (final int x : new int[] {MID_X - 2, MID_X + 2}) {
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
    Furnishing.chandelier(t, MID_X, TOP - 1, FLIGHT_FOOT + 2);
    Furnishing.chandelier(t, MID_X, TOP - 1, GALLERY_FRONT - 3);
    for (int x = MID_X - 2; x <= MID_X + 2; x++) {
      for (int z = FLIGHT_FOOT + 1; z <= ROOM_S; z++) {
        final boolean border = x == MID_X - 2 || x == MID_X + 2;
        t.set(x, F0 + 1, z, border ? "black_carpet" : "red_carpet");
      }
    }
    Furnishing.chair(t, WEST, F0 + 1, ROOM_S, "east");
    Furnishing.table(t, WEST, F0 + 1, ROOM_S - 1);
    Furnishing.candelabra(t, WEST, F0 + 2, ROOM_S - 1);
    Furnishing.chair(t, EAST, F0 + 1, ROOM_S, "west");
    Furnishing.table(t, EAST, F0 + 1, ROOM_S - 1);
    Furnishing.candelabra(t, EAST, F0 + 2, ROOM_S - 1);
    for (final int z : new int[] {30, 37}) {
      Furnishing.portrait(t, WEST, F0 + 3, z, "east");
      Furnishing.portrait(t, EAST, F0 + 3, z, "west");
    }
    for (final int z : new int[] {28, 31, 38}) {
      Furnishing.portrait(t, WEST, F1 + 3, z, "east");
      Furnishing.portrait(t, EAST, F1 + 3, z, "west");
    }
    for (final int x : new int[] {WEST + 1, EAST - 1, MID_X - 3, MID_X + 3}) {
      Furnishing.portrait(t, x, F0 + 2, LANDING_FRONT + 1, "south");
    }
    Furnishing.candelabra(t, WEST, LANDING + 1, ROOM_N);
    Furnishing.candelabra(t, EAST, LANDING + 1, ROOM_N);
  }
}
