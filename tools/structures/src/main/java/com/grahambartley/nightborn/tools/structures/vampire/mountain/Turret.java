package com.grahambartley.nightborn.tools.structures.vampire.mountain;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import java.util.List;

public record Turret(int cx, int cz, int radius, int base, int top, boolean round, int walk) {

  public static final int STOREY = 6;
  public static final int ROOF_LAYERS = 2;

  public boolean inside(final int x, final int z, final int r) {
    return Spire.within(x - cx, z - cz, r, round);
  }

  public boolean wall(final int x, final int z) {
    return inside(x, z, radius) && !inside(x, z, radius - 1);
  }

  public boolean floor(final int y) {
    return y >= walk && y < top && (y - walk) % STOREY == 0;
  }

  public int roofTop() {
    return top + Spire.height(radius + 1, ROOF_LAYERS);
  }

  public void build(final Template t) {
    for (int x = cx - radius; x <= cx + radius; x++) {
      for (int z = cz - radius; z <= cz + radius; z++) {
        if (!inside(x, z, radius)) {
          continue;
        }
        Crag.column(t, x, z, base);
        for (int y = base + 1; y <= top; y++) {
          if (wall(x, z)) {
            t.set(x, y, z, MountainCastle.WALL.at(x, y, z));
          } else if (y == top || floor(y)) {
            t.set(x, y, z, MountainCastle.PLANKS);
          } else {
            t.set(x, y, z, BlockState.AIR);
          }
        }
      }
    }
    windows(t);
    doors(t);
    ladder(t);
    Spire.raise(t, cx, cz, radius + 1, top + 1, round, ROOF_LAYERS);
  }

  private void windows(final Template t) {
    for (int y = base + 3; y < top - 1; y = y < walk ? walk + 3 : y + STOREY) {
      for (final int[] side : new int[][] {{radius, 0}, {-radius, 0}, {0, radius}, {0, -radius}}) {
        t.set(cx + side[0], y, cz + side[1], MountainCastle.PANE);
        t.set(cx + side[0], y + 1, cz + side[1], MountainCastle.PANE);
      }
    }
  }

  private record Climb(int x, int z, int backX, int backZ, String facing) {}

  private List<Climb> climbs() {
    return List.of(
        new Climb(cx - radius + 1, cz - 1, -1, 0, "east"),
        new Climb(cx + radius - 1, cz + 1, 1, 0, "west"),
        new Climb(cx + 1, cz - radius + 1, 0, -1, "south"),
        new Climb(cx - 1, cz + radius - 1, 0, 1, "north"));
  }

  public static boolean solid(final String state) {
    return state != null
        && !BlockState.AIR.equals(state)
        && !state.contains("pane")
        && !state.contains("glass")
        && !state.contains("bars");
  }

  private boolean solidBehind(final Template t, final Climb climb) {
    for (int y = base + 1; y < top; y++) {
      if (!solid(t.get(climb.x() + climb.backX(), y, climb.z() + climb.backZ()))) {
        return false;
      }
    }
    return true;
  }

  private void ladder(final Template t) {
    final Climb climb =
        climbs().stream()
            .filter(c -> solidBehind(t, c))
            .findFirst()
            .orElseThrow(
                () -> new IllegalStateException("No solid wall for the ladder in " + this));
    for (int y = base + 1; y < top; y++) {
      t.set(
          climb.x(),
          y,
          climb.z(),
          BlockState.of("ladder", "facing", climb.facing(), "waterlogged", false));
    }
  }

  private void doors(final Template t) {
    final int towardX = Integer.signum(MountainCastle.MID - cx);
    final int towardZ = Integer.signum(Ramparts.heart(base) - cz);
    final int[][] sides = {
      {towardX * radius, 0}, {0, towardZ * radius}, {towardX * (radius - 1), towardZ * (radius - 1)}
    };
    for (final int[] side : sides) {
      final int x = cx + side[0];
      final int z = cz + side[1];
      if (wall(x, z) && Crag.level(x, z) != Crag.NONE && !Ramparts.onWall(x, z)) {
        opening(t, x, z, base);
        break;
      }
    }
    for (int x = cx - radius; x <= cx + radius; x++) {
      for (int z = cz - radius; z <= cz + radius; z++) {
        if (wall(x, z) && Ramparts.onWalk(x, z)) {
          opening(t, x, z, walk);
        }
      }
    }
  }

  private void opening(final Template t, final int x, final int z, final int floor) {
    t.set(x, floor + 1, z, BlockState.AIR);
    t.set(x, floor + 2, z, BlockState.AIR);
  }
}
