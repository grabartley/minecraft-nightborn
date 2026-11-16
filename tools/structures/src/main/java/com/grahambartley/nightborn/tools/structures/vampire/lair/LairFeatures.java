package com.grahambartley.nightborn.tools.structures.vampire.lair;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Facing;
import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Axis;
import com.grahambartley.nightborn.tools.structures.vampire.Furnishing;
import com.grahambartley.nightborn.tools.structures.vampire.Openings;
import com.grahambartley.nightborn.tools.structures.vampire.Stone;

public final class LairFeatures {

  public static final String DEEPSLATE_COFFIN = "nightborn:deepslate_brick_coffin";
  public static final String BLACKSTONE_COFFIN = "nightborn:polished_blackstone_brick_coffin";
  public static final String POOL = "nightborn:blood_pool";
  static final Stone ROCK = new Stone("deepslate", "cobbled_deepslate", "tuff", 45);
  static final Stone DRESSED =
      new Stone("deepslate_bricks", "deepslate_tiles", "cracked_deepslate_bricks", 30);
  static final String ARCH = "deepslate_brick_stairs";
  static final String KERB =
      BlockState.of("polished_blackstone_brick_slab", "type", "bottom", "waterlogged", false);
  static final String RIM = "polished_blackstone_bricks";
  static final int SHRINE_HEIGHT = 5;
  static final int WALL_HEIGHT = 3;
  static final int STALACTITE_ROOF = 6;
  static final int STALACTITE_PERCENT = 6;

  private LairFeatures() {}

  public static void cave(final Template t) {
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

  public static void shrine(
      final Template t, final int x, final int z, final String front, final String coffin) {
    final Pos out = Facing.STEP.get(front);
    final Axis wall = out.x() == 0 ? Axis.X : Axis.Z;
    final int depth0 = wall == Axis.X ? z : x;
    final int along = wall == Axis.X ? x : z;
    final int sign = wall == Axis.X ? out.z() : out.x();
    for (int d = -2; d <= 0; d++) {
      for (int a = along - 2; a <= along + 2; a++) {
        final int line = depth0 + sign * (d + 1);
        for (int y = 1; y <= SHRINE_HEIGHT - Math.abs(a - along) / 2; y++) {
          final int cx = wall.x(a, line);
          final int cz = wall.z(a, line);
          t.set(
              cx,
              y,
              cz,
              d == 0 && Math.abs(a - along) <= 2 && y <= 4
                  ? DRESSED.at(cx, y, cz)
                  : ROCK.at(cx, y, cz));
        }
      }
    }
    for (int d = -1; d <= 0; d++) {
      Openings.arch(t, wall, along - 1, along + 1, depth0 + sign * (d + 1), 1, 3, ARCH);
    }
    Furnishing.standingCoffin(
        t, coffin, wall.x(along, depth0), 1, wall.z(along, depth0), Facing.OPPOSITE.get(front));
  }

  public static void pool(final Template t, final int cx, final int cz, final double radius) {
    final int reach = (int) Math.ceil(radius + 1);
    for (int x = cx - reach; x <= cx + reach; x++) {
      for (int z = cz - reach; z <= cz + reach; z++) {
        final double r = Math.hypot(x - cx, z - cz);
        if (r <= radius) {
          t.set(x, 0, z, POOL);
        } else if (r <= radius + 1) {
          t.set(x, 0, z, RIM);
          t.set(x, 1, z, KERB);
        }
      }
    }
  }

  public static void stalagmite(final Template t, final int x, final int z, final int height) {
    for (int y = 1; y < height; y++) {
      t.set(x, y, z, "dripstone_block");
    }
    t.set(x, height, z, dripstone("frustum"));
    t.set(x, height + 1, z, dripstone("tip"));
  }

  public static void bones(final Template t, final int x, final int z, final int skullRotation) {
    t.set(x, 1, z, BlockState.of("bone_block", "axis", "x"));
    t.set(x + 1, 1, z, BlockState.of("skeleton_skull", "rotation", skullRotation));
    t.set(x, 2, z, BlockState.of("skeleton_skull", "rotation", (skullRotation + 3) % 16));
  }

  public static String splatter(final String facing, final int splat) {
    return BlockState.of(
        "nightborn:blood_splatter", "face", "floor", "facing", facing, "splat", splat);
  }

  public static void path(
      final Template t, final int x0, final int z0, final int x1, final int z1) {
    for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
      for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) {
        t.set(x, 0, z, Stone.roll(x, 0, z) < 25 ? "cracked_deepslate_tiles" : "polished_deepslate");
      }
    }
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

  private static String dripstone(final String thickness) {
    return BlockState.of(
        "pointed_dripstone",
        "thickness",
        thickness,
        "vertical_direction",
        "up",
        "waterlogged",
        false);
  }
}
