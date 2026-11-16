package com.grahambartley.nightborn.tools.structures.vampire.lair;

import static com.grahambartley.nightborn.tools.structures.vampire.lair.LairShrine.BLACKSTONE_COFFIN;
import static com.grahambartley.nightborn.tools.structures.vampire.lair.LairShrine.DEEPSLATE_COFFIN;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Furnishing;

public final class Grotto {

  public static final String PATH = "vampire/lair/grotto";
  public static final int SIZE_X = 31;
  public static final int SIZE_Y = 12;
  public static final int SIZE_Z = 15;
  public static final int LEDGE = 22;
  public static final int LEDGE_TOP = 2;
  public static final int[] BENCHES = {4, 7, 10};
  public static final int SPRING_X = 14;
  public static final int SPRING_Z = 7;
  static final double SPRING_REACH_X = 5.5;
  static final double SPRING_REACH_Z = 2;

  private Grotto() {}

  public static Template build() {
    final Template t = new Template(PATH, SIZE_X, SIZE_Y, SIZE_Z);
    LairCave.carve(t);
    LairFeatures.path(t, 11, 10, LEDGE - 1, 11);
    LairFeatures.path(t, LEDGE - 1, 6, LEDGE - 1, 11);
    spring(t);
    ledge(t);
    LairShrine.cut(t, 6, 1, "south", DEEPSLATE_COFFIN);
    LairShrine.cut(t, 15, 13, "north", BLACKSTONE_COFFIN);
    hearth(t);
    LairFeatures.readingCorner(t, 2, 4, 7, "east");
    LairFeatures.bones(t, 2, 3, 4);
    LairFeatures.stalagmite(t, 3, 12, 2);
    LairFeatures.stalagmite(t, 12, 2, 1);
    LairFeatures.stalagmite(t, 19, 12, 2);
    return t;
  }

  private static void spring(final Template t) {
    LairFeatures.pool(t, SPRING_X, SPRING_Z, SPRING_REACH_X, SPRING_REACH_Z);
    t.set(
        SPRING_X + 6,
        1,
        SPRING_Z,
        BlockState.of("nightborn:blood_fountain", "facing", "west", "flowing", true));
    t.set(SPRING_X, 1, SPRING_Z - 3, BlockState.AIR);
    t.set(SPRING_X, 1, SPRING_Z + 3, BlockState.AIR);
  }

  private static void ledge(final Template t) {
    LairCave.ROCK.fill(t, LEDGE, 0, 2, SIZE_X - 1, LEDGE_TOP, 12);
    LairShrine.DRESSED.fill(t, LEDGE, 0, 2, LEDGE, LEDGE_TOP, 12);
    for (int z = 6; z <= 8; z++) {
      t.set(LEDGE - 1, 1, z, step());
      t.set(LEDGE, 2, z, step());
      t.set(LEDGE, 1, z, LairShrine.DRESSED.base());
    }
    final int y = LEDGE_TOP + 1;
    for (int i = 0; i < BENCHES.length; i++) {
      Furnishing.coffin(
          t, i % 2 == 0 ? BLACKSTONE_COFFIN : DEEPSLATE_COFFIN, 24, y, BENCHES[i], "east");
    }
    t.set(27, y, 7, "polished_blackstone");
    Furnishing.chalice(t, 27, y + 1, 7, "villager");
    Furnishing.candelabra(t, 27, y, 5);
    Furnishing.candelabra(t, 27, y, 9);
    Furnishing.chest(t, 27, y, 3, "west");
  }

  private static void hearth(final Template t) {
    Furnishing.table(t, 11, 1, 11);
    Furnishing.chair(t, 10, 1, 11, "east");
    Furnishing.chair(t, 12, 1, 11, "west");
    Furnishing.candles(t, 11, 2, 11, 3, false);
    t.set(9, 1, 12, Furnishing.splatter("south", 2));
    t.set(13, 1, 3, Furnishing.splatter("east", 1));
  }

  private static String step() {
    return BlockState.of(
        "deepslate_brick_stairs",
        "facing",
        "east",
        "half",
        "bottom",
        "shape",
        "straight",
        "waterlogged",
        false);
  }
}
