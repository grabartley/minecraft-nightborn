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
  static final Stone DRESSED =
      new Stone("deepslate_bricks", "deepslate_tiles", "cracked_deepslate_bricks", 30);
  static final String ARCH = "deepslate_brick_stairs";
  static final String KERB =
      BlockState.of("polished_blackstone_brick_slab", "type", "bottom", "waterlogged", false);
  static final String RIM = "polished_blackstone_bricks";
  static final int SHRINE_HEIGHT = 5;

  private LairFeatures() {}

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
                  : LairCave.ROCK.at(cx, y, cz));
        }
      }
    }
    for (int d = -1; d <= 0; d++) {
      Openings.arch(t, wall, along - 1, along + 1, depth0 + sign * (d + 1), 1, 3, ARCH);
    }
    Furnishing.standingCoffin(
        t, coffin, wall.x(along, depth0), 1, wall.z(along, depth0), Facing.OPPOSITE.get(front));
  }

  public static void readingCorner(
      final Template t, final int shelfX, final int lecternX, final int z, final String toward) {
    for (int dz = -1; dz <= 1; dz++) {
      t.set(shelfX, 1, z + dz, "bookshelf");
      t.set(shelfX, 2, z + dz, "bookshelf");
    }
    t.set(
        lecternX,
        1,
        z,
        BlockState.of("lectern", "facing", toward, "has_book", false, "powered", false));
    final int step = Integer.signum(lecternX - shelfX);
    Furnishing.chair(t, lecternX + step, 1, z, Facing.OPPOSITE.get(toward));
    Furnishing.candles(t, shelfX + step, 1, z + 1, 2, false);
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

  public static void path(
      final Template t, final int x0, final int z0, final int x1, final int z1) {
    for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
      for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) {
        t.set(x, 0, z, Stone.roll(x, 0, z) < 25 ? "cracked_deepslate_tiles" : "polished_deepslate");
      }
    }
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
