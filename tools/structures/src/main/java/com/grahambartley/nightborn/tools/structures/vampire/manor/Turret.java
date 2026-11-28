package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.BURGUNDY;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.F0;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.F1;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.HOUSE_E;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.HOUSE_S;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.ROOM_E;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.ROOM_S;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.TOP;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.RouteRoom;
import java.util.ArrayList;
import java.util.List;

public record Turret(boolean round) {

  public static final int CX = HOUSE_E;
  public static final int CZ = HOUSE_S;
  public static final int RADIUS = 4;
  public static final int STUDY = TOP;
  public static final int WALL_TOP = STUDY + 9;
  public static final int STAIR_X = CX + 1;
  public static final int STAIR_FOOT_Z = CZ + RADIUS - 1;
  public static final int STAIR_TOP_Z = STAIR_FOOT_Z - (STUDY - F1 - 1);

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

  public static boolean inRoom(final int x, final int z) {
    return x <= ROOM_E && z <= ROOM_S;
  }

  public static boolean floorRow(final int y) {
    return y == F0 || y == F1 || y == STUDY || y == WALL_TOP;
  }

  public int roofTop() {
    return WALL_TOP + 2 + 2 * RADIUS;
  }

  public List<Window> windows() {
    final List<Window> all = new ArrayList<>();
    final int diagonalX = round ? CX + RADIUS - 1 : CX + RADIUS;
    final int diagonalZ = round ? CZ + RADIUS - 2 : CZ + RADIUS - 1;
    for (final int[] sill : new int[][] {{F0 + 2, 4}, {F1 + 2, 3}, {STUDY + 2, 4}}) {
      all.add(new Window(CX + RADIUS, CZ, "east", sill[0], sill[1], Manor.PANE, BURGUNDY));
      all.add(new Window(CX - 1, CZ + RADIUS, "south", sill[0], sill[1], Manor.PANE, BURGUNDY));
      all.add(new Window(diagonalX, diagonalZ, "east", sill[0], sill[1], Manor.STAINED, BURGUNDY));
    }
    return all;
  }

  public void build(final Template t) {
    for (int x = CX - RADIUS; x <= CX + RADIUS; x++) {
      for (int z = CZ - RADIUS; z <= CZ + RADIUS; z++) {
        if (inside(x, z, RADIUS)) {
          column(t, x, z);
        }
      }
    }
    windows().forEach(window -> window.cut(t));
    roof(t);
    stair(t);
  }

  private void column(final Template t, final int x, final int z) {
    final boolean open = inRoom(x, z);
    for (int y = 0; y <= WALL_TOP; y++) {
      final boolean shell = ring(x, z) && !(open && y < TOP && !floorRow(y));
      if (y < F0 || (shell && floorRow(y))) {
        t.set(x, y, z, Manor.DRESSING.at(x, y, z));
      } else if (shell) {
        t.set(x, y, z, Manor.WALL.at(x, y, z));
      } else if (floorRow(y)) {
        t.set(x, y, z, Manor.PLANKS);
      } else {
        t.set(x, y, z, BlockState.AIR);
      }
    }
  }

  private void stair(final Template t) {
    for (int i = 0; i <= STUDY - F1 - 1; i++) {
      final int y = F1 + 1 + i;
      final int z = STAIR_FOOT_Z - i;
      for (int below = F1 + 1; below < y; below++) {
        t.set(STAIR_X, below, z, Manor.PLANKS);
      }
      t.set(STAIR_X, y, z, Parts.stair("dark_oak_stairs", "north"));
      for (int above = y + 1; above <= y + RouteRoom.HOUSEHOLD + 1; above++) {
        t.set(STAIR_X, above, z, BlockState.AIR);
      }
    }
    for (int z = STAIR_TOP_Z + 1; z < STAIR_FOOT_Z; z++) {
      for (final int x : new int[] {STAIR_X - 1, STAIR_X + 1}) {
        if (inside(x, z, RADIUS - 1)) {
          t.set(x, STUDY + 1, z, Parts.fence());
        }
      }
    }
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
          t.set(x, y, z, Manor.SLATE.full());
        } else {
          t.set(
              x, y, z, full ? Manor.SLATE.full() : Parts.stair(Manor.SLATE.stairs(), inward(x, z)));
        }
      }
    }
  }
}
