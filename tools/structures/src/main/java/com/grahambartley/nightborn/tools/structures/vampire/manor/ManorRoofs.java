package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.HOUSE_E;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.HOUSE_N;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.HOUSE_S;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.HOUSE_W;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.MID_X;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.SLATE;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.TOP;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Axis;
import com.grahambartley.nightborn.tools.structures.vampire.Roof;

public final class ManorRoofs {

  public static final int GABLE_W = MID_X - 4;
  public static final int GABLE_E = MID_X + 4;
  public static final int GABLE_BACK = HOUSE_S - 6;
  public static final int RIDGE_Z = (HOUSE_N + HOUSE_S) / 2;
  public static final String FINIAL = "lightning_rod";

  private ManorRoofs() {}

  public static int rise(final int z) {
    return Math.max(0, Math.min(z - (HOUSE_N - 1), HOUSE_S + 1 - z));
  }

  public static void build(final Template t) {
    final int ridge =
        Roof.gable(t, Axis.X, HOUSE_W, HOUSE_E, HOUSE_N, HOUSE_S, TOP, SLATE, Manor.WALL);
    for (int x = GABLE_W; x <= GABLE_E; x++) {
      t.set(x, TOP, HOUSE_S + 1, BlockState.AIR);
    }
    final int gableRidge =
        Roof.gable(t, Axis.Z, GABLE_BACK, HOUSE_S, GABLE_W, GABLE_E, TOP, SLATE, Manor.WALL, false);
    finial(t, HOUSE_W, ridge, RIDGE_Z);
    finial(t, HOUSE_E, ridge, RIDGE_Z);
    finial(t, MID_X, gableRidge, HOUSE_S);
    Roof.fillAttic(t, HOUSE_W + 1, HOUSE_N + 1, HOUSE_E - 1, HOUSE_S - 1, TOP + 1, Manor.PLANKS);
  }

  private static void finial(final Template t, final int x, final int ridge, final int z) {
    t.set(
        x, ridge, z, BlockState.of(FINIAL, "facing", "up", "powered", false, "waterlogged", false));
  }
}
