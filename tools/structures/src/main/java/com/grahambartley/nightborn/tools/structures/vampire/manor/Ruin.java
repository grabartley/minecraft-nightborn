package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.F1;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.TOP;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Stone;

public final class Ruin {

  public static final double HOLE_X = 50.5;
  public static final double HOLE_Z = 19.5;
  public static final double HOLE_RADIUS = 2.8;
  public static final ManorShell.Box FOOTPRINT = ManorShell.SITTING_ROOM;
  public static final int RAFTER_X = 51;
  public static final int FALLEN_BEAM_X = 48;
  public static final int FALLEN_BEAM_Z = 20;

  private Ruin() {}

  public static boolean fallen(final int x, final int z) {
    final double jitter = Stone.roll(x, 31, z) / 100.0 - 0.5;
    return Math.hypot(x - HOLE_X, z - HOLE_Z) + jitter <= HOLE_RADIUS;
  }

  public static void fallenRoof(final Template t) {
    final ManorShell.Box room = FOOTPRINT;
    for (int x = room.x0(); x <= room.x1(); x++) {
      for (int z = room.z0(); z <= room.z1(); z++) {
        if (!fallen(x, z)) {
          continue;
        }
        for (int y = TOP; y < t.sizeY(); y++) {
          t.set(x, y, z, BlockState.AIR);
        }
        if (BlockState.AIR.equals(t.get(x, F1 + 1, z))) {
          t.set(x, F1 + 1, z, rubble(x, z));
        }
      }
    }
    for (int x = FALLEN_BEAM_X; x <= FALLEN_BEAM_X + 2; x++) {
      if (fallen(x, FALLEN_BEAM_Z)) {
        t.set(x, F1 + 1, FALLEN_BEAM_Z, Parts.log(Manor.BEAM, "x"));
      }
    }
    for (int z = room.z0(); z <= room.z0() + 4; z++) {
      t.set(RAFTER_X, TOP + 2, z, Parts.log("dark_oak_log", "z"));
    }
  }

  static String rubble(final int x, final int z) {
    final int roll = Stone.roll(x, F1 + 1, z);
    if (roll < 25) {
      return Parts.slab(Manor.SLATE.slab(), "bottom");
    }
    if (roll < 40) {
      return Parts.slab("nightborn:cracked_gothic_stone_brick_slab", "bottom");
    }
    if (roll < 60) {
      return "moss_carpet";
    }
    return BlockState.AIR;
  }
}
