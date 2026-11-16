package com.grahambartley.nightborn.tools.structures.vampire.manor;

import com.grahambartley.nightborn.tools.structures.template.Facing;
import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Stone;
import java.util.List;

public final class Ivy {

  public static final int PATCH = 3;
  public static final int SPREAD = 30;
  public static final int GAPS = 25;
  public static final int LOWEST = 1;
  public static final int REACH = Manor.TOP + 3;
  static final List<String> SIDES = List.of("north", "south", "east", "west");

  private Ivy() {}

  public static boolean climbs(final String state) {
    return state != null
        && (state.startsWith("minecraft:bricks")
            || state.startsWith("nightborn:") && state.contains("gothic_stone_bricks"));
  }

  public static int height(final int x, final int z) {
    return LOWEST + 2 + Stone.roll(x, 11, z) % (REACH - 2);
  }

  public static boolean patch(final int x, final int z) {
    return Stone.roll(Math.floorDiv(x, PATCH), 7, Math.floorDiv(z, PATCH)) < SPREAD;
  }

  public static void grow(final Template t) {
    for (int x = 0; x < t.sizeX(); x++) {
      for (int z = 0; z < t.sizeZ(); z++) {
        if (!patch(x, z)) {
          continue;
        }
        for (int y = LOWEST; y <= height(x, z); y++) {
          if (t.get(x, y, z) != null || Stone.roll(x, y, z) < GAPS) {
            continue;
          }
          for (final String side : SIDES) {
            final Pos step = Facing.STEP.get(side);
            final int nx = x + step.x();
            final int nz = z + step.z();
            if (t.inside(nx, y, nz) && climbs(t.get(nx, y, nz))) {
              t.set(x, y, z, Parts.vine(side));
              break;
            }
          }
        }
      }
    }
  }
}
