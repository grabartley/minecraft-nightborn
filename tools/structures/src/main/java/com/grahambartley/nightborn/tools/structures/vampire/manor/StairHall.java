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

public final class StairHall {

  public static final int WEST = WING_E + 1;
  public static final int EAST = HALL_E - 1;
  public static final int LANDING = F0 + 3;
  public static final int LANDING_BACK = MAIN_N + 4;
  public static final int LANDING_FRONT = LANDING_BACK + 3;
  public static final int GALLERY_FRONT = MAIN_S - 2;
  public static final int FLIGHT_FOOT = LANDING_FRONT + 2;
  public static final String FENCE = "dark_oak_fence";
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
      t.set(x, LANDING - 1, FLIGHT_FOOT - 1, Parts.stair(STEP, "north"));
      t.set(x, LANDING - 2, FLIGHT_FOOT, Parts.stair(STEP, "north"));
      t.set(x, LANDING - 2, FLIGHT_FOOT - 1, Manor.PLANKS);
    }
    for (final int x0 : new int[] {WEST, MID_X + 2}) {
      for (int x = x0; x < x0 + 3; x++) {
        t.set(x, LANDING + 1, LANDING_BACK + 1, Parts.stair(STEP, "south"));
        t.set(x, LANDING, LANDING_BACK + 1, Manor.PLANKS);
        t.set(x, LANDING + 2, LANDING_BACK + 2, Parts.stair(STEP, "south"));
        t.set(x, LANDING + 1, LANDING_BACK + 2, Manor.PLANKS);
        t.set(x, LANDING, LANDING_BACK + 2, Manor.PLANKS);
      }
    }
  }

  private static void galleries(final Template t) {
    for (int z = LANDING_FRONT; z < MAIN_S; z++) {
      for (int x = WEST; x <= EAST; x++) {
        final boolean side = x < MID_X - 1 || x > MID_X + 1;
        if (side || z >= GALLERY_FRONT) {
          t.set(x, F1, z, Manor.PLANKS);
        }
      }
    }
  }

  private static void balustrade(final Template t) {
    for (int z = LANDING_FRONT; z < GALLERY_FRONT; z++) {
      t.set(MID_X - 2, F1 + 1, z, fence(true, false));
      t.set(MID_X + 2, F1 + 1, z, fence(true, false));
    }
    for (int x = MID_X - 1; x <= MID_X + 1; x++) {
      t.set(x, F1 + 1, GALLERY_FRONT, fence(false, true));
    }
    t.set(
        MID_X - 2,
        F1 + 1,
        GALLERY_FRONT,
        BlockState.of(
            FENCE,
            "north",
            true,
            "east",
            true,
            "south",
            false,
            "west",
            false,
            "waterlogged",
            false));
    t.set(
        MID_X + 2,
        F1 + 1,
        GALLERY_FRONT,
        BlockState.of(
            FENCE,
            "north",
            true,
            "west",
            true,
            "south",
            false,
            "east",
            false,
            "waterlogged",
            false));
    for (final int x : new int[] {MID_X - 2, MID_X + 2}) {
      t.set(x, F0 + 1, FLIGHT_FOOT, "stripped_dark_oak_log");
      t.set(x, F0 + 2, FLIGHT_FOOT, Parts.lantern(false));
    }
  }

  static String fence(final boolean northSouth, final boolean eastWest) {
    return BlockState.of(
        FENCE,
        "north",
        northSouth,
        "south",
        northSouth,
        "east",
        eastWest,
        "west",
        eastWest,
        "waterlogged",
        false);
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
      t.set(WEST, F0 + 3, z, portrait("east"));
      t.set(EAST, F0 + 3, z, portrait("west"));
      t.set(WEST, F1 + 2, z, portrait("east"));
      t.set(EAST, F1 + 2, z, portrait("west"));
    }
    Furnishing.candelabra(t, WEST, LANDING + 1, MAIN_N + 1);
    Furnishing.candelabra(t, EAST, LANDING + 1, MAIN_N + 1);
    Furnishing.drape(t, Manor.BURGUNDY, MID_X - 2, LANDING + 2, MAIN_N + 1, 4, "south");
    Furnishing.drape(t, Manor.BURGUNDY, MID_X + 2, LANDING + 2, MAIN_N + 1, 4, "south");
  }

  static String portrait(final String facing) {
    return BlockState.of(Manor.PORTRAIT, "facing", facing);
  }
}
