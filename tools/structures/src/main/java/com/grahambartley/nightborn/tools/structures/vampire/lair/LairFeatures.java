package com.grahambartley.nightborn.tools.structures.vampire.lair;

import com.grahambartley.nightborn.tools.structures.nbt.Nbt;
import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Facing;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Furnishing;
import com.grahambartley.nightborn.tools.structures.vampire.Stone;

public final class LairFeatures {

  public static final String POOL = "nightborn:blood_pool";
  public static final String BOOK = "minecraft:writable_book";
  static final String KERB =
      BlockState.of("polished_blackstone_brick_slab", "type", "bottom", "waterlogged", false);
  static final String RIM = "polished_blackstone_bricks";

  private LairFeatures() {}

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
        BlockState.of("lectern", "facing", toward, "has_book", true, "powered", false),
        Nbt.compound(
            "id", "minecraft:lectern", "Book", Nbt.compound("id", BOOK, "count", 1), "Page", 0));
    final int step = Integer.signum(lecternX - shelfX);
    Furnishing.chair(t, lecternX + step, 1, z, Facing.OPPOSITE.get(toward));
    Furnishing.candles(t, shelfX + step, 1, z + 1, 2, false);
  }

  public static void pool(
      final Template t, final int cx, final int cz, final double rx, final double rz) {
    final int reachX = (int) Math.ceil(rx) + 1;
    final int reachZ = (int) Math.ceil(rz) + 1;
    for (int x = cx - reachX; x <= cx + reachX; x++) {
      for (int z = cz - reachZ; z <= cz + reachZ; z++) {
        if (inPool(x - cx, z - cz, rx, rz)) {
          t.set(x, 0, z, POOL);
        } else if (bordersPool(x - cx, z - cz, rx, rz)) {
          t.set(x, 0, z, RIM);
          t.set(x, 1, z, KERB);
        }
      }
    }
  }

  private static boolean inPool(final int dx, final int dz, final double rx, final double rz) {
    return Math.hypot(dx / rx, dz / rz) <= 1;
  }

  private static boolean bordersPool(final int dx, final int dz, final double rx, final double rz) {
    for (int sx = -1; sx <= 1; sx++) {
      for (int sz = -1; sz <= 1; sz++) {
        if (inPool(dx + sx, dz + sz, rx, rz)) {
          return true;
        }
      }
    }
    return false;
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
