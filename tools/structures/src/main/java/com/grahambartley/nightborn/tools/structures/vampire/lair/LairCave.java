package com.grahambartley.nightborn.tools.structures.vampire.lair;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Stone;

public final class LairCave {

  static final Stone ROCK = new Stone("deepslate", "cobbled_deepslate", "tuff", 45);
  static final int WALL_HEIGHT = 3;
  static final int STALACTITE_ROOF = 6;
  static final int STALACTITE_PERCENT = 6;

  private LairCave() {}

  public static void carve(final Template t) {
    ROCK.fill(t, 0, 0, 0, t.sizeX() - 1, 0, t.sizeZ() - 1);
    for (int x = 0; x < t.sizeX(); x++) {
      for (int z = 0; z < t.sizeZ(); z++) {
        final int roof = roof(t, x, z);
        for (int y = 1; y < t.sizeY(); y++) {
          t.set(x, y, z, y <= roof ? BlockState.AIR : ROCK.at(x, y, z));
        }
        if (roof >= STALACTITE_ROOF && Stone.roll(x, roof, z) < STALACTITE_PERCENT) {
          t.set(x, roof, z, hanging("tip"));
          if (Stone.roll(z, roof, x) < STALACTITE_PERCENT * 4) {
            t.set(x, roof, z, hanging("frustum"));
            t.set(x, roof - 1, z, hanging("tip"));
          }
        }
      }
    }
  }

  public static int roof(final Template t, final int x, final int z) {
    final double rx = (t.sizeX() - 1) / 2.0;
    final double rz = (t.sizeZ() - 1) / 2.0;
    final double wobble = (Stone.roll(x, 0, z) % 10) / 100.0;
    final double reach = Math.pow(Math.abs(x - rx) / rx, 4) + Math.pow(Math.abs(z - rz) / rz, 4);
    if (reach + wobble > 1) {
      return 0;
    }
    final int rise = t.sizeY() - 2 - WALL_HEIGHT;
    return WALL_HEIGHT + (int) Math.round(rise * Math.sqrt(1 - reach)) - Stone.roll(z, 1, x) % 2;
  }

  private static String hanging(final String thickness) {
    return BlockState.of(
        "pointed_dripstone",
        "thickness",
        thickness,
        "vertical_direction",
        "down",
        "waterlogged",
        false);
  }
}
