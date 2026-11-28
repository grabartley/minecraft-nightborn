package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.F0;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.HOUSE_S;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.MID_X;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;

public final class ManorFront {

  public static final int TERRACE_W = MID_X - 5;
  public static final int TERRACE_E = MID_X + 5;
  public static final int TERRACE_S = HOUSE_S + 4;
  public static final int STEPS_HALF = 3;
  public static final int STEP_Z = TERRACE_S + 1;
  public static final int STEPS_END = STEP_Z + F0 - 1;
  public static final int PORCH_TOP = F0 + 6;
  public static final String STEP = Manor.TRIM;
  public static final String STEP_FILL = "nightborn:gothic_stone_bricks";

  private ManorFront() {}

  public static boolean onSteps(final int x) {
    return Math.abs(x - MID_X) <= STEPS_HALF;
  }

  public static void build(final Template t) {
    terrace(t);
    porch(t);
    steps(t);
  }

  private static void terrace(final Template t) {
    for (int x = TERRACE_W; x <= TERRACE_E; x++) {
      for (int z = HOUSE_S + 1; z <= TERRACE_S; z++) {
        for (int y = 0; y < F0; y++) {
          t.set(x, y, z, Manor.PLINTH.at(x, y, z));
        }
        t.set(x, F0, z, (x + z) % 2 == 0 ? "polished_deepslate" : "nightborn:carved_gothic_stone");
        for (int y = F0 + 1; y <= PORCH_TOP + 1; y++) {
          t.set(x, y, z, BlockState.AIR);
        }
        final boolean side = x == TERRACE_W || x == TERRACE_E;
        final boolean front = z == TERRACE_S && !onSteps(x);
        if (side || front) {
          t.set(x, F0 + 1, z, Manor.TRIM_WALL);
        }
      }
    }
  }

  private static void porch(final Template t) {
    for (final int x : new int[] {TERRACE_W, TERRACE_E}) {
      for (final int z : new int[] {HOUSE_S + 1, TERRACE_S}) {
        for (int y = F0 + 1; y < PORCH_TOP; y++) {
          t.set(x, y, z, Manor.DRESSING.at(x, y, z));
        }
      }
    }
    for (int x = TERRACE_W; x <= TERRACE_E; x++) {
      for (int z = HOUSE_S + 1; z <= TERRACE_S; z++) {
        final boolean edge = x == TERRACE_W || x == TERRACE_E || z == TERRACE_S;
        t.set(x, PORCH_TOP, z, edge ? Manor.DRESSING.at(x, PORCH_TOP, z) : Manor.SLATE.full());
        t.set(x, PORCH_TOP + 1, z, edge ? Manor.TRIM_WALL : BlockState.AIR);
      }
    }
    for (int x = TERRACE_W + 1; x < TERRACE_E; x++) {
      t.set(x, PORCH_TOP - 1, TERRACE_S, Parts.hood(Manor.TRIM, "south"));
    }
    t.set(MID_X, PORCH_TOP - 1, TERRACE_S - 2, Parts.lantern(true));
    for (final int x : new int[] {TERRACE_W, TERRACE_E}) {
      t.set(x, PORCH_TOP + 2, TERRACE_S, BlockState.of(Manor.GARGOYLE, "facing", "south"));
    }
  }

  private static void steps(final Template t) {
    for (int x = MID_X - STEPS_HALF; x <= MID_X + STEPS_HALF; x++) {
      Parts.flight(t, STEP, STEP_FILL, x, STEP_Z, "north", F0, F0, 0);
    }
    for (final int x : new int[] {MID_X - STEPS_HALF - 1, MID_X + STEPS_HALF + 1}) {
      for (int z = STEP_Z; z <= STEPS_END; z++) {
        final int tread = F0 - (z - STEP_Z);
        for (int y = 0; y <= tread; y++) {
          t.set(x, y, z, Manor.PLINTH.at(x, y, z));
        }
        t.set(x, tread + 1, z, Parts.slab(Manor.TRIM_SLAB, "bottom"));
      }
    }
  }
}
