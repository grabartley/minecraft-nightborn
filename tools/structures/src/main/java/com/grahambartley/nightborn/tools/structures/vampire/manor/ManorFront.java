package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.DRESSING;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.F0;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.F1;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.MAIN_N;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.MAIN_S;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.MID_X;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.WING_S;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Axis;
import com.grahambartley.nightborn.tools.structures.vampire.Openings;

public final class ManorFront {

  public static final int PORCH_W = MID_X - 3;
  public static final int PORCH_E = MID_X + 3;
  public static final int PORCH_S = MAIN_S + 4;
  public static final int PORCH_ROOF = F0 + 5;
  public static final int STEP_Z = PORCH_S + 1;
  public static final int BAY_W = 15;
  public static final int BAY_E = 19;
  public static final int BAY_S = WING_S + 2;
  public static final int GREAT_SILL = F1 + 1;
  public static final int STAIR_SILL = F0 + 5;

  private ManorFront() {}

  public static void build(final Template t) {
    porch(t);
    steps(t);
    bay(t);
    greatWindow(t);
    stairWindow(t);
  }

  private static void porch(final Template t) {
    for (int x = PORCH_W; x <= PORCH_E; x++) {
      for (int z = MAIN_S + 1; z <= PORCH_S; z++) {
        for (int y = 0; y <= PORCH_ROOF; y++) {
          final boolean side = x == PORCH_W || x == PORCH_E || z == PORCH_S;
          if (y <= F0 || y == PORCH_ROOF || side) {
            t.set(x, y, z, DRESSING.at(x, y, z));
          } else {
            t.set(x, y, z, BlockState.AIR);
          }
        }
        t.set(x, F0, z, (x + z) % 2 == 0 ? "polished_deepslate" : "nightborn:carved_gothic_stone");
        final boolean edge = x == PORCH_W || x == PORCH_E || z == PORCH_S;
        t.set(x, PORCH_ROOF + 1, z, edge ? Manor.TRIM_WALL : BlockState.AIR);
      }
    }
    Openings.arch(t, Axis.X, MID_X - 1, MID_X + 1, PORCH_S, F0 + 1, 4, Manor.TRIM);
    Openings.arch(t, Axis.Z, MAIN_S + 2, MAIN_S + 3, PORCH_W, F0 + 2, 2, Manor.TRIM);
    Openings.arch(t, Axis.Z, MAIN_S + 2, MAIN_S + 3, PORCH_E, F0 + 2, 2, Manor.TRIM);
    for (final int x : new int[] {PORCH_W, PORCH_E}) {
      t.set(x, PORCH_ROOF + 2, PORCH_S, Parts.lantern(false));
    }
    t.set(MID_X, PORCH_ROOF - 1, MAIN_S + 2, Parts.lantern(true));
    Window.door(t, MID_X, F0 + 1, MAIN_S, "south");
    for (final int x : new int[] {MID_X - 1, MID_X + 1}) {
      for (int y = F0 + 1; y <= F0 + 3; y++) {
        t.set(x, y, MAIN_S, Manor.STAINED);
      }
    }
    t.set(MID_X, F0 + 3, MAIN_S, Manor.STAINED);
  }

  private static void steps(final Template t) {
    for (int x = MID_X - 1; x <= MID_X + 1; x++) {
      t.set(x, 2, STEP_Z, Parts.stair(Manor.TRIM, "north"));
      t.set(x, 1, STEP_Z + 1, Parts.stair(Manor.TRIM, "north"));
      t.set(x, 1, STEP_Z, DRESSING.at(x, 1, STEP_Z));
      t.set(x, 0, STEP_Z, DRESSING.at(x, 0, STEP_Z));
      t.set(x, 0, STEP_Z + 1, DRESSING.at(x, 0, STEP_Z + 1));
    }
    for (final int x : new int[] {MID_X - 2, MID_X + 2}) {
      for (int z = STEP_Z; z <= STEP_Z + 1; z++) {
        for (int y = 0; y <= 2; y++) {
          t.set(x, y, z, DRESSING.at(x, y, z));
        }
      }
      t.set(x, 3, STEP_Z, Parts.slab(Manor.TRIM_SLAB, "bottom"));
      t.set(x, 3, STEP_Z + 1, BlockState.of(Manor.GARGOYLE, "facing", "south"));
    }
  }

  private static void bay(final Template t) {
    for (int x = BAY_W; x <= BAY_E; x++) {
      for (int z = WING_S; z <= BAY_S; z++) {
        if (!inBay(x, z)) {
          continue;
        }
        final boolean rim = !inBay(x - 1, z) || !inBay(x + 1, z) || !inBay(x, z + 1);
        for (int y = 0; y <= F1; y++) {
          if (y < F0) {
            t.set(x, y, z, DRESSING.at(x, y, z));
          } else if (z == WING_S && (x == BAY_W || x == BAY_E)) {
            t.set(x, y, z, y == F1 ? DRESSING.at(x, y, z) : Manor.BRICK.at(x, y, z));
          } else if (y == F0) {
            t.set(x, y, z, rim ? DRESSING.at(x, y, z) : Manor.PLANKS);
          } else if (y == F1) {
            t.set(x, y, z, DRESSING.at(x, y, z));
          } else if (rim && (y == F0 + 1 || y == F1 - 1)) {
            t.set(x, y, z, DRESSING.at(x, y, z));
          } else if (rim) {
            t.set(x, y, z, Manor.STAINED);
          } else {
            t.set(x, y, z, BlockState.AIR);
          }
        }
        t.set(x, F1 + 1, z, rim ? Manor.TRIM_WALL : Parts.slab(Manor.TRIM_SLAB, "bottom"));
      }
    }
  }

  static boolean inBay(final int x, final int z) {
    if (z < WING_S || z > BAY_S || x < BAY_W || x > BAY_E) {
      return false;
    }
    return z < BAY_S || (x > BAY_W && x < BAY_E);
  }

  private static void greatWindow(final Template t) {
    for (int x = MID_X - 1; x <= MID_X + 1; x++) {
      final int top = x == MID_X ? GREAT_SILL + 4 : GREAT_SILL + 2;
      for (int y = GREAT_SILL; y <= top; y++) {
        t.set(x, y, MAIN_S, Manor.STAINED);
      }
    }
  }

  private static void stairWindow(final Template t) {
    for (int x = MID_X - 1; x <= MID_X + 1; x++) {
      final int top = x == MID_X ? STAIR_SILL + 4 : STAIR_SILL + 3;
      for (int y = STAIR_SILL; y <= top; y++) {
        t.set(x, y, MAIN_N, Manor.STAINED);
      }
    }
  }
}
