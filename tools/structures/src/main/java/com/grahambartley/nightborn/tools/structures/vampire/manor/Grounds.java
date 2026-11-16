package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.MAIN_N;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.MID_X;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.SIZE_X;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.SIZE_Z;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Stone;

public final class Grounds {

  public static final int RAILING_Z = SIZE_Z - 2;
  public static final int SWEEP_Z = 35;
  public static final double SWEEP_INNER = 3.5;
  public static final double SWEEP_OUTER = 5.3;
  public static final int BACK_PATH_Z = MAIN_N - 3;
  public static final int WEST_PATH_X = 1;
  public static final int EAST_PATH_X = SIZE_X - 3;
  public static final int HEDGE_HEIGHT = 2;

  private Grounds() {}

  public static void build(final Template t) {
    drive(t);
    paths(t);
    railing(t);
    hedges(t);
    Topiary.figure(t, MID_X, SWEEP_Z);
    Topiary.cone(t, MID_X - 4, RAILING_Z - 2);
    Topiary.cone(t, MID_X + 4, RAILING_Z - 2);
    lamp(t, MID_X - 4, ManorFront.STEP_Z + 2);
    lamp(t, MID_X + 4, ManorFront.STEP_Z + 2);
    deadTree(t, 7, 33);
    deadTree(t, 41, 36);
    kitchenYard(t);
    lawn(t);
  }

  public static boolean onSweep(final int x, final int z) {
    final double d = Math.hypot(x - MID_X, z - SWEEP_Z);
    return d >= SWEEP_INNER && d <= SWEEP_OUTER;
  }

  private static void ground(final Template t, final int x, final int z, final String state) {
    if (t.inside(x, 0, z) && t.get(x, 0, z) == null) {
      t.set(x, 0, z, state);
    }
  }

  private static void drive(final Template t) {
    for (int x = 0; x < SIZE_X; x++) {
      for (int z = ManorFront.STEP_Z + 2; z <= RAILING_Z; z++) {
        final boolean lane = Math.abs(x - MID_X) <= 1 && !inCircle(x, z);
        if (lane || onSweep(x, z)) {
          ground(t, x, z, Stone.roll(x, 0, z) < 25 ? "coarse_dirt" : "gravel");
        }
      }
    }
  }

  private static boolean inCircle(final int x, final int z) {
    return Math.hypot(x - MID_X, z - SWEEP_Z) < SWEEP_INNER;
  }

  private static void paths(final Template t) {
    for (int x = WEST_PATH_X; x <= EAST_PATH_X; x++) {
      path(t, x, BACK_PATH_Z);
    }
    for (int z = BACK_PATH_Z; z <= RAILING_Z - 1; z++) {
      path(t, WEST_PATH_X, z);
      path(t, EAST_PATH_X, z);
    }
    for (int x = WEST_PATH_X; x <= EAST_PATH_X; x++) {
      if (Math.abs(x - MID_X) > SWEEP_OUTER) {
        path(t, x, SWEEP_Z);
      }
    }
    for (int z = BACK_PATH_Z + 1; z < MAIN_N; z++) {
      path(t, MID_X, z);
      path(t, 36, z);
    }
    for (int x = WEST_PATH_X; x < Glasshouse.WEST - 2; x++) {
      path(t, x, Glasshouse.DOOR_Z);
    }
  }

  private static void path(final Template t, final int x, final int z) {
    ground(t, x, z, Stone.roll(x, 0, z) < 22 ? "grass_block" : "dirt_path");
  }

  private static void railing(final Template t) {
    for (int x = 1; x < SIZE_X - 1; x++) {
      if (Math.abs(x - MID_X) <= 1) {
        continue;
      }
      final boolean pier = Math.abs(x - MID_X) == 2 || Math.floorMod(x - MID_X, 6) == 2;
      t.set(x, 0, RAILING_Z, Manor.DRESSING.at(x, 0, RAILING_Z));
      t.set(x, 1, RAILING_Z, Manor.BRICK.at(x, 1, RAILING_Z));
      if (pier) {
        final int top = Math.abs(x - MID_X) == 2 ? 4 : 2;
        for (int y = 2; y <= top; y++) {
          t.set(x, y, RAILING_Z, Manor.BRICK.at(x, y, RAILING_Z));
        }
        t.set(x, top + 1, RAILING_Z, Parts.slab(Manor.TRIM_SLAB, "bottom"));
        if (top == 4) {
          t.set(x, top + 2, RAILING_Z, Parts.lantern(false));
        }
      } else {
        t.set(x, 2, RAILING_Z, "iron_bars");
        t.set(x, 3, RAILING_Z, Stone.roll(x, 3, RAILING_Z) < 20 ? BlockState.AIR : "iron_bars");
      }
    }
  }

  private static void hedges(final Template t) {
    for (int z = 1; z < RAILING_Z; z++) {
      hedge(t, 0, z);
      hedge(t, SIZE_X - 1, z);
    }
    for (int x = 0; x < SIZE_X; x++) {
      hedge(t, x, 0);
    }
  }

  private static void hedge(final Template t, final int x, final int z) {
    if (Stone.roll(x, 1, z) < 12) {
      return;
    }
    ground(t, x, z, "grass_block");
    for (int y = 1; y <= HEDGE_HEIGHT; y++) {
      if (y < HEDGE_HEIGHT || Stone.roll(x, y, z) < 70) {
        t.set(x, y, z, Parts.leaves("spruce_leaves"));
      }
    }
  }

  private static void lamp(final Template t, final int x, final int z) {
    for (int y = 1; y <= 3; y++) {
      t.set(x, y, z, "polished_blackstone_wall");
    }
    t.set(x, 4, z, Parts.lantern(false));
  }

  private static void deadTree(final Template t, final int x, final int z) {
    for (int y = 1; y <= 7; y++) {
      t.set(x, y, z, Parts.log("dark_oak_log", "y"));
    }
    t.set(x + 1, 5, z, Parts.log("dark_oak_log", "x"));
    t.set(x + 2, 6, z, Parts.log("dark_oak_log", "x"));
    t.set(x, 6, z - 1, Parts.log("dark_oak_log", "z"));
    t.set(x, 7, z - 2, Parts.log("dark_oak_log", "z"));
    t.set(x - 1, 8, z, Parts.log("dark_oak_log", "x"));
    ground(t, x, z, "podzol");
    t.set(x + 1, 1, z + 1, "dead_bush");
    ground(t, x + 1, z + 1, "podzol");
  }

  private static void kitchenYard(final Template t) {
    for (final int x : new int[] {MID_X, 36}) {
      t.set(x, 2, MAIN_N - 1, Parts.stair(Manor.TRIM, "south"));
      t.set(x, 1, MAIN_N - 2, Parts.stair(Manor.TRIM, "south"));
      t.set(x, 1, MAIN_N - 1, Manor.DRESSING.at(x, 1, MAIN_N - 1));
      t.set(x, 0, MAIN_N - 1, Manor.DRESSING.at(x, 0, MAIN_N - 1));
      t.set(x, 0, MAIN_N - 2, Manor.DRESSING.at(x, 0, MAIN_N - 2));
    }
    for (int x = 38; x <= 40; x++) {
      for (int y = 1; y <= 2; y++) {
        t.set(x, y, 2, Parts.log("oak_log", "x"));
      }
    }
    t.set(37, 1, 3, Parts.log("stripped_oak_log", "y"));
    t.set(33, 1, 3, BlockState.of("barrel", "facing", "up", "open", false));
    t.set(33, 1, 4, BlockState.of("barrel", "facing", "up", "open", false));
    t.set(34, 1, 3, BlockState.of("water_cauldron", "level", 2));
    t.set(32, 1, 3, "composter");
  }

  private static void lawn(final Template t) {
    for (int x = 0; x < SIZE_X; x++) {
      for (int z = 0; z < SIZE_Z; z++) {
        final int roll = Stone.roll(x, 0, z);
        if (roll < 12) {
          ground(t, x, z, "podzol");
        } else if (roll < 20) {
          ground(t, x, z, "coarse_dirt");
        } else if (roll < 28) {
          ground(t, x, z, "moss_block");
        } else {
          ground(t, x, z, BlockState.of("grass_block", "snowy", false));
        }
        if (t.get(x, 1, z) == null && t.get(x, 0, z).contains("grass_block")) {
          final int plant = Stone.roll(x, 1, z);
          if (plant < 14) {
            t.set(x, 1, z, plant < 6 ? "fern" : "short_grass");
          }
        }
        for (int y = 1; y < t.sizeY(); y++) {
          if (t.get(x, y, z) == null) {
            t.set(x, y, z, BlockState.AIR);
          }
        }
      }
    }
  }
}
