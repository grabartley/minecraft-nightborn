package com.grahambartley.nightborn.tools.structures.vampire.mountain;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Axis;
import com.grahambartley.nightborn.tools.structures.vampire.Openings;
import com.grahambartley.nightborn.tools.structures.vampire.manor.Parts;
import java.util.List;
import java.util.stream.IntStream;

public final class Donjon {

  public static final int HALF = 8;
  public static final int WEST = MountainCastle.MID - HALF;
  public static final int EAST = MountainCastle.MID + HALF;
  public static final int NORTH = MountainCastle.MID - HALF;
  public static final int SOUTH = MountainCastle.MID + HALF;
  public static final int STOREY = 6;
  public static final int STOREYS = 7;
  public static final int GROUND = MountainCastle.UP;
  public static final int EYRIE = GROUND + STOREY * (STOREYS - 1);
  public static final int TERRACE = GROUND + STOREY * STOREYS;
  public static final int ROOF = TERRACE + 4;
  public static final int FLIGHT_FOOT = SOUTH - 3;
  public static final int HEADROOM = 3;
  public static final int HATCH_X = MountainCastle.MID + 3;

  private Donjon() {}

  public static int floor(final int storey) {
    return GROUND + STOREY * storey;
  }

  public static int flightX(final int storey) {
    return storey % 2 == 0 ? WEST + 1 : EAST - 1;
  }

  public static List<Integer> floors() {
    return IntStream.range(0, STOREYS).map(Donjon::floor).boxed().toList();
  }

  public static void build(final Template t) {
    shell(t);
    for (int storey = 0; storey < STOREYS - 1; storey++) {
      flight(t, storey);
    }
    windows(t);
    Openings.arch(
        t,
        Axis.X,
        MountainCastle.MID - 1,
        MountainCastle.MID + 1,
        SOUTH,
        GROUND + 1,
        4,
        MountainCastle.KEEP_STEP);
    crown(t);
  }

  private static void shell(final Template t) {
    for (int x = WEST; x <= EAST; x++) {
      for (int z = NORTH; z <= SOUTH; z++) {
        final boolean wall = x == WEST || x == EAST || z == NORTH || z == SOUTH;
        Crag.column(t, x, z, GROUND);
        for (int y = GROUND + 1; y <= TERRACE; y++) {
          if (wall) {
            t.set(x, y, z, MountainCastle.KEEP.at(x, y, z));
          } else if (y == TERRACE) {
            t.set(x, y, z, MountainCastle.KEEP.at(x, y, z));
          } else if ((y - GROUND) % STOREY == 0) {
            t.set(x, y, z, MountainCastle.PLANKS);
          } else {
            t.set(x, y, z, BlockState.AIR);
          }
        }
        if (!wall) {
          t.set(x, GROUND, z, (x + z) % 2 == 0 ? "polished_andesite" : "polished_deepslate");
        }
      }
    }
  }

  private static void flight(final Template t, final int storey) {
    final int x = flightX(storey);
    final int from = floor(storey);
    for (int i = 0; i < STOREY; i++) {
      final int z = FLIGHT_FOOT - i;
      final int y = from + 1 + i;
      t.set(x, y, z, Parts.stair(MountainCastle.KEEP_STEP, "north"));
      for (int above = y + 1; above <= y + HEADROOM; above++) {
        t.set(x, above, z, BlockState.AIR);
      }
    }
    final int rail = x == WEST + 1 ? x + 1 : x - 1;
    for (int z = FLIGHT_FOOT - STOREY + 2; z <= FLIGHT_FOOT - 2; z++) {
      t.set(rail, from + STOREY + 1, z, Parts.fence());
    }
  }

  private static void windows(final Template t) {
    for (int storey = 0; storey < STOREYS; storey++) {
      final int y = floor(storey) + 2;
      for (final int offset : new int[] {-4, 4}) {
        Openings.lancet(
            t, Axis.X, MountainCastle.MID + offset, NORTH, y, 2, MountainCastle.STAINED);
        Openings.lancet(t, Axis.Z, MountainCastle.MID + offset, WEST, y, 2, MountainCastle.STAINED);
        Openings.lancet(t, Axis.Z, MountainCastle.MID + offset, EAST, y, 2, MountainCastle.STAINED);
        if (storey > 0) {
          Openings.lancet(
              t, Axis.X, MountainCastle.MID + offset, SOUTH, y, 2, MountainCastle.STAINED);
        }
      }
    }
  }

  private static void crown(final Template t) {
    final int ring = HALF + 1;
    for (int dx = -ring; dx <= ring; dx++) {
      for (int dz = -ring; dz <= ring; dz++) {
        if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) {
          continue;
        }
        final int x = MountainCastle.MID + dx;
        final int z = MountainCastle.MID + dz;
        t.set(x, TERRACE - 1, z, corbel(dx, dz, ring));
        t.set(x, TERRACE, z, MountainCastle.KEEP.at(x, TERRACE, z));
        t.set(x, TERRACE + 1, z, MountainCastle.KEEP.at(x, TERRACE + 1, z));
        if ((dx + dz) % 2 == 0) {
          t.set(x, TERRACE + 2, z, MountainCastle.KEEP.at(x, TERRACE + 2, z));
        }
      }
    }
    t.fill(
        MountainCastle.MID - HALF,
        TERRACE + 1,
        MountainCastle.MID - HALF,
        MountainCastle.MID + HALF,
        ROOF - 1,
        MountainCastle.MID + HALF,
        BlockState.AIR);
    for (final int dx : new int[] {-HALF, HALF}) {
      for (final int dz : new int[] {-HALF, HALF}) {
        for (int y = TERRACE + 1; y < ROOF; y++) {
          t.set(MountainCastle.MID + dx, y, MountainCastle.MID + dz, MountainCastle.TIMBER);
        }
      }
    }
    for (int y = TERRACE + 1; y < ROOF; y++) {
      t.set(MountainCastle.MID, y, MountainCastle.MID, "chiseled_stone_bricks");
    }
    t.set(MountainCastle.MID, ROOF - 1, MountainCastle.MID - 1, Parts.lantern(true));
    for (int dx = -HALF; dx <= HALF; dx++) {
      for (int dz = -HALF; dz <= HALF; dz++) {
        t.set(MountainCastle.MID + dx, ROOF, MountainCastle.MID + dz, MountainCastle.PLANKS);
      }
    }
    Spire.raise(t, MountainCastle.MID, MountainCastle.MID, HALF + 1, ROOF, false, 1);
    for (int r = HALF; r >= 1; r--) {
      final int y = ROOF + HALF + 1 - r;
      t.fill(
          MountainCastle.MID - r + 1,
          y,
          MountainCastle.MID - r + 1,
          MountainCastle.MID + r - 1,
          y,
          MountainCastle.MID + r - 1,
          MountainCastle.PLANKS);
    }
    for (int y = EYRIE + 1; y <= TERRACE; y++) {
      t.set(
          HATCH_X, y, SOUTH - 1, BlockState.of("ladder", "facing", "north", "waterlogged", false));
    }
  }

  static String corbel(final int dx, final int dz, final int ring) {
    if (Math.abs(dx) == ring && Math.abs(dz) == ring) {
      return MountainCastle.KEEP.base();
    }
    final String facing =
        Math.abs(dx) == ring ? (dx > 0 ? "west" : "east") : (dz > 0 ? "north" : "south");
    return Parts.hood(MountainCastle.KEEP_STEP, facing);
  }
}
