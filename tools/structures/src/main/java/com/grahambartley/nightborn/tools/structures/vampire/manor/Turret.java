package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.F0;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.F1;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.MAIN_E;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.MAIN_S;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.TOP;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;

public record Turret(boolean round) {

  public static final int CX = MAIN_E;
  public static final int CZ = MAIN_S;
  public static final int RADIUS = 3;
  public static final int STUDY = TOP;
  public static final int WALL_TOP = STUDY + 6;
  public static final int LADDER_Z = CZ + 2;

  public boolean inside(final int x, final int z, final int r) {
    if (r < 0) {
      return false;
    }
    final int dx = x - CX;
    final int dz = z - CZ;
    return round ? dx * dx + dz * dz <= r * r + r : Math.max(Math.abs(dx), Math.abs(dz)) <= r;
  }

  public boolean ring(final int x, final int z) {
    return inside(x, z, RADIUS) && !inside(x, z, RADIUS - 1);
  }

  public static boolean arch(final int y) {
    return y == F0 + 1 || y == F0 + 2 || y == F1 + 1 || y == F1 + 2;
  }

  public static boolean doorway(final int x, final int z) {
    final int dx = x - (CX - 2);
    final int dz = z - (CZ - 2);
    return !outsideHouse(x, z) && dx * dx + dz * dz <= 1;
  }

  public static boolean outsideHouse(final int x, final int z) {
    return x >= MAIN_E || z >= MAIN_S;
  }

  public int roofTop() {
    return WALL_TOP + 2 + 2 * RADIUS;
  }

  public void build(final Template t) {
    for (int x = CX - RADIUS; x <= CX + RADIUS; x++) {
      for (int z = CZ - RADIUS; z <= CZ + RADIUS; z++) {
        if (inside(x, z, RADIUS)) {
          column(t, x, z);
        }
      }
    }
    windows(t);
    roof(t);
    for (int y = F1 + 1; y <= STUDY; y++) {
      t.set(
          ladderX(), y, LADDER_Z, BlockState.of("ladder", "facing", "east", "waterlogged", false));
    }
  }

  private void column(final Template t, final int x, final int z) {
    for (int y = 0; y <= WALL_TOP; y++) {
      final boolean shell = ring(x, z) && !(doorway(x, z) && arch(y));
      final boolean floor = y == F0 || y == F1 || y == STUDY || y == WALL_TOP;
      if (y < F0 || (shell && floor)) {
        t.set(x, y, z, Manor.DRESSING.at(x, y, z));
      } else if (shell) {
        t.set(x, y, z, Manor.BRICK.at(x, y, z));
      } else {
        t.set(x, y, z, floor ? Manor.PLANKS : BlockState.AIR);
      }
    }
  }

  private void windows(final Template t) {
    final int[][] faces = {{RADIUS, 0, 0}, {0, RADIUS, 0}, {RADIUS, RADIUS, 1}};
    for (final int[] face : faces) {
      final int x = face[2] == 1 ? CX + diagonal() : CX + face[0];
      final int z = face[2] == 1 ? CZ + diagonal() : CZ + face[1];
      for (final int sill : new int[] {F0 + 2, F1 + 1, STUDY + 2}) {
        for (int y = sill; y < sill + 3; y++) {
          t.set(x, y, z, face[2] == 1 && sill == F0 + 2 ? Manor.STAINED : Manor.PANE);
        }
      }
    }
  }

  public int ladderX() {
    return round ? CX - 1 : CX - 2;
  }

  private int diagonal() {
    return round ? 2 : RADIUS;
  }

  public static String inward(final int x, final int z) {
    final int dx = x - CX;
    final int dz = z - CZ;
    if (Math.abs(dx) > Math.abs(dz)) {
      return dx > 0 ? "west" : "east";
    }
    return dz > 0 ? "north" : "south";
  }

  private void roof(final Template t) {
    int y = WALL_TOP + 1;
    ring(t, RADIUS + 1, y++, false);
    for (int r = RADIUS; r >= 1; r--) {
      ring(t, r, y++, true);
      ring(t, r, y++, false);
    }
    t.set(CX, y, CZ, Manor.SLATE.full());
    t.set(
        CX,
        y + 1,
        CZ,
        BlockState.of(ManorRoofs.FINIAL, "facing", "up", "powered", false, "waterlogged", false));
  }

  private void ring(final Template t, final int r, final int y, final boolean full) {
    for (int x = CX - r; x <= CX + r; x++) {
      for (int z = CZ - r; z <= CZ + r; z++) {
        if (!inside(x, z, r)) {
          continue;
        }
        if (inside(x, z, r - 1)) {
          t.set(x, y, z, BlockState.AIR);
        } else {
          t.set(
              x, y, z, full ? Manor.SLATE.full() : Parts.stair(Manor.SLATE.stairs(), inward(x, z)));
        }
      }
    }
  }
}
