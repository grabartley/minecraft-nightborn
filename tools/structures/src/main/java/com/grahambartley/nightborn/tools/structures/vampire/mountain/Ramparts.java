package com.grahambartley.nightborn.tools.structures.vampire.mountain;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Axis;
import com.grahambartley.nightborn.tools.structures.vampire.Openings;
import com.grahambartley.nightborn.tools.structures.vampire.RouteRoom;
import com.grahambartley.nightborn.tools.structures.vampire.manor.Parts;

public final class Ramparts {

  public static final int THICK = 2;
  public static final int RISE = 8;
  public static final int TOP = MountainCastle.UP + RISE;
  public static final int LOW_TOP = MountainCastle.LOW + RISE;
  public static final int GATE_WEST = MountainCastle.MID - 2;
  public static final int GATE_EAST = MountainCastle.MID + 2;
  public static final int GATE_HEIGHT = 5;
  public static final int WALK_HEADROOM = RouteRoom.HOUSEHOLD;
  public static final int STAIR_FOOT = Crag.UPPER_SOUTH + 6;
  public static final int LOWER_HEART = 51;

  private Ramparts() {}

  public static int heart(final int base) {
    return base == MountainCastle.UP ? MountainCastle.MID : LOWER_HEART;
  }

  private static int depth(final int x, final int z) {
    final int level = Crag.level(x, z);
    if (level == MountainCastle.UP) {
      return Math.min(
          Math.min(x - Crag.UPPER_WEST, Crag.UPPER_EAST - x),
          Math.min(z - Crag.UPPER_NORTH, Crag.UPPER_SOUTH - z));
    }
    if (level == MountainCastle.LOW) {
      return Math.min(Math.min(x - Crag.LOWER_WEST, Crag.LOWER_EAST - x), Crag.LOWER_SOUTH - z);
    }
    return THICK;
  }

  public static boolean onWall(final int x, final int z) {
    return depth(x, z) < THICK;
  }

  public static boolean outerFace(final int x, final int z) {
    return depth(x, z) == 0;
  }

  public static boolean onWalk(final int x, final int z) {
    return depth(x, z) == THICK - 1;
  }

  public static int top(final int x, final int z) {
    return Crag.level(x, z) + RISE;
  }

  public static void build(final Template t) {
    for (int x = 0; x < MountainCastle.SIZE; x++) {
      for (int z = 0; z < MountainCastle.SIZE; z++) {
        if (Crag.level(x, z) == Crag.NONE || !onWall(x, z)) {
          continue;
        }
        final int level = Crag.level(x, z);
        final int top = top(x, z);
        Crag.column(t, x, z, level + 1);
        for (int y = level + 2; y <= top; y++) {
          t.set(x, y, z, MountainCastle.WALL.at(x, y, z));
        }
        for (int y = top + 1; y <= top + WALK_HEADROOM; y++) {
          t.set(x, y, z, BlockState.AIR);
        }
        if (outerFace(x, z)) {
          crenel(t, x, z, top + 1);
        }
      }
    }
    innerGate(t);
    grandStair(t);
  }

  public static void crenel(final Template t, final int x, final int z, final int y) {
    t.set(x, y, z, MountainCastle.WALL.at(x, y, z));
    if ((x + z) % 2 == 0) {
      t.set(x, y + 1, z, MountainCastle.WALL.at(x, y + 1, z));
    }
  }

  private static void innerGate(final Template t) {
    for (int z = Crag.UPPER_SOUTH - THICK + 1; z <= Crag.UPPER_SOUTH; z++) {
      Openings.arch(
          t,
          Axis.X,
          GATE_WEST,
          GATE_EAST,
          z,
          MountainCastle.UP + 1,
          GATE_HEIGHT,
          MountainCastle.TRIM);
      for (int x = GATE_WEST; x <= GATE_EAST; x++) {
        t.set(x, MountainCastle.UP, z, MountainCastle.FOOT.at(x, MountainCastle.UP, z));
      }
    }
    t.set(
        MountainCastle.MID, MountainCastle.UP + GATE_HEIGHT, Crag.UPPER_SOUTH, Parts.lantern(true));
  }

  private static void grandStair(final Template t) {
    final int steps = MountainCastle.UP - MountainCastle.LOW;
    for (int i = 0; i < steps; i++) {
      final int z = STAIR_FOOT - i;
      final int y = MountainCastle.LOW + 1 + i;
      for (int x = GATE_WEST - 1; x <= GATE_EAST + 1; x++) {
        final boolean cheek = x < GATE_WEST || x > GATE_EAST;
        for (int below = MountainCastle.LOW + 1; below < y; below++) {
          t.set(x, below, z, MountainCastle.FOOT.at(x, below, z));
        }
        t.set(
            x,
            y,
            z,
            cheek
                ? MountainCastle.FOOT.at(x, y, z)
                : Parts.stair(MountainCastle.KEEP_STEP, "north"));
        if (cheek) {
          t.set(x, y + 1, z, BlockState.of("stone_brick_wall", "waterlogged", false));
        }
      }
    }
  }
}
