package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.MAIN_E;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.MAIN_N;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.MAIN_S;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.MID_X;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.SLATE;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.TOP;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.WING_E;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.WING_S;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.WING_W;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Axis;
import com.grahambartley.nightborn.tools.structures.vampire.Roof;

public final class ManorRoofs {

  public static final int DORMER_W = MID_X - 3;
  public static final int DORMER_E = MID_X + 3;
  public static final int DORMER_BACK = MAIN_S - 3;
  public static final String FINIAL = "lightning_rod";

  private ManorRoofs() {}

  public static void build(final Template t) {
    final int wingRidge =
        Roof.gable(t, Axis.Z, MAIN_N, WING_S, WING_W, WING_E, TOP, SLATE, Manor.BRICK);
    final int mainRidge =
        Roof.gable(t, Axis.X, WING_E, MAIN_E, MAIN_N, MAIN_S, TOP, SLATE, Manor.BRICK);
    for (int z = MAIN_N; z <= MAIN_S; z++) {
      final boolean wall = z == MAIN_N || z == MAIN_S;
      t.set(WING_E + 1, TOP, z, wall ? Manor.DRESSING.at(WING_E + 1, TOP, z) : Manor.PLANKS);
    }
    t.set(WING_E, TOP, MAIN_S + 1, Manor.DRESSING.at(WING_E, TOP, MAIN_S + 1));
    for (int x = DORMER_W; x <= DORMER_E; x++) {
      t.set(x, TOP, MAIN_S + 1, BlockState.AIR);
    }
    final int dormerRidge =
        Roof.gable(
            t, Axis.Z, DORMER_BACK, MAIN_S, DORMER_W, DORMER_E, TOP, SLATE, Manor.BRICK, false);
    finial(t, (WING_W + WING_E) / 2, wingRidge, WING_S);
    finial(t, (WING_W + WING_E) / 2, wingRidge, MAIN_N);
    finial(t, MID_X, dormerRidge, MAIN_S);
    finial(t, MAIN_E, mainRidge, (MAIN_N + MAIN_S) / 2);
    attic(t);
    Roof.fillAttic(t, WING_W + 1, MAIN_N + 1, WING_E - 1, WING_S - 1, TOP + 1, Manor.PLANKS);
    Roof.fillAttic(t, WING_E + 1, MAIN_N + 1, MAIN_E - 1, MAIN_S - 1, TOP + 1, Manor.PLANKS);
  }

  private static void finial(final Template t, final int x, final int ridge, final int z) {
    t.set(
        x, ridge, z, BlockState.of(FINIAL, "facing", "up", "powered", false, "waterlogged", false));
  }

  private static void attic(final Template t) {
    final int mid = (WING_W + WING_E) / 2;
    for (final int z : new int[] {MAIN_N, WING_S}) {
      t.set(mid, TOP + 2, z, Manor.PANE);
      t.set(mid, TOP + 3, z, Manor.PANE);
    }
    t.set(MID_X, TOP + 2, MAIN_S, Manor.STAINED);
  }
}
