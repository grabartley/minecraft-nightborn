package com.grahambartley.nightborn.tools.structures.vampire.crypt;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Stone;

public final class CryptStair {

  public static final String GROUND = "nightborn:crypt_ground";
  public static final String POOL = "nightborn:vampire/crypt/church";
  public static final int SIZE_X = 15;
  public static final int SIZE_Y = 7;
  public static final int SIZE_Z = 15;
  public static final int TOP = SIZE_Y - 1;
  public static final int OFFSET_X = Church.STAIR_X - (SIZE_X - 1);
  public static final int OFFSET_Z = Church.STAIR_Z - 2;
  public static final int STAIR_Z = Church.STAIR_Z - OFFSET_Z;
  public static final int FOOT_X = 8;
  public static final int ANCHOR_X = 7;
  public static final int ANCHOR_Z = 8;
  static final String STEP = "nightborn:gothic_stone_brick_stairs";
  static final String RIB = "tuff_brick_stairs";
  static final String SHAFT = "tuff_brick_wall";
  static final String CAPITAL = "chiseled_tuff";
  static final Stone FLAGS = new Stone("polished_tuff", "tuff_bricks", "tuff", 35);

  static final Stone MASONRY =
      new Stone(
          "nightborn:gothic_stone_bricks",
          "nightborn:mossy_gothic_stone_bricks",
          "nightborn:cracked_gothic_stone_bricks",
          22);

  private CryptStair() {}

  public static Template shell(final String path) {
    final Template t = new Template(path, SIZE_X, SIZE_Y, SIZE_Z);
    MASONRY.fill(t, 0, 0, 0, SIZE_X - 1, TOP, SIZE_Z - 1);
    FLAGS.fill(t, 1, 0, 1, SIZE_X - 2, 0, SIZE_Z - 2);
    for (int x = FOOT_X; x < SIZE_X; x++) {
      final int step = x - FOOT_X;
      for (int y = Math.max(1, step); y <= Math.min(TOP, step + 3); y++) {
        t.set(x, y, STAIR_Z, BlockState.AIR);
      }
      if (step > 0 && step < TOP) {
        t.set(x, step, STAIR_Z, step());
      }
    }
    t.jigsaw(
        SIZE_X - 1,
        TOP,
        STAIR_Z,
        "up_north",
        Template.EMPTY,
        Church.VESTRY,
        POOL,
        step(),
        "aligned");
    t.anchor(ANCHOR_X, TOP, ANCHOR_Z, GROUND, MASONRY.base());
    return t;
  }

  static String step() {
    return BlockState.of(
        STEP, "facing", "east", "half", "bottom", "shape", "straight", "waterlogged", false);
  }
}
