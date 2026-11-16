package com.grahambartley.nightborn.tools.structures.vampire.lair;

import com.grahambartley.nightborn.tools.structures.template.Facing;
import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Axis;
import com.grahambartley.nightborn.tools.structures.vampire.Furnishing;
import com.grahambartley.nightborn.tools.structures.vampire.Openings;
import com.grahambartley.nightborn.tools.structures.vampire.Stone;

public final class LairShrine {

  public static final String DEEPSLATE_COFFIN = "nightborn:deepslate_brick_coffin";
  public static final String BLACKSTONE_COFFIN = "nightborn:polished_blackstone_brick_coffin";
  static final Stone DRESSED =
      new Stone("deepslate_bricks", "deepslate_tiles", "cracked_deepslate_bricks", 30);
  static final String ARCH = "deepslate_brick_stairs";
  static final int HEIGHT = 5;

  private LairShrine() {}

  public static void cut(
      final Template t, final int x, final int z, final String front, final String coffin) {
    final Pos out = Facing.STEP.get(front);
    final Axis wall = out.x() == 0 ? Axis.X : Axis.Z;
    final int depth0 = wall == Axis.X ? z : x;
    final int along = wall == Axis.X ? x : z;
    final int sign = wall == Axis.X ? out.z() : out.x();
    for (int d = -2; d <= 0; d++) {
      for (int a = along - 2; a <= along + 2; a++) {
        final int line = depth0 + sign * (d + 1);
        final int cx = wall.x(a, line);
        final int cz = wall.z(a, line);
        final int top = HEIGHT - Math.abs(a - along) / 2;
        for (int y = 1; y <= top; y++) {
          t.set(cx, y, cz, d == 0 && y <= 4 ? DRESSED.at(cx, y, cz) : LairCave.ROCK.at(cx, y, cz));
        }
        final String above = t.get(cx, top + 1, cz);
        if (above != null && above.contains("pointed_dripstone")) {
          t.set(cx, top + 1, cz, LairCave.ROCK.at(cx, top + 1, cz));
        }
      }
    }
    for (int d = -1; d <= 0; d++) {
      Openings.arch(t, wall, along - 1, along + 1, depth0 + sign * (d + 1), 1, 3, ARCH);
    }
    Furnishing.standingCoffin(
        t, coffin, wall.x(along, depth0), 1, wall.z(along, depth0), Facing.OPPOSITE.get(front));
  }
}
