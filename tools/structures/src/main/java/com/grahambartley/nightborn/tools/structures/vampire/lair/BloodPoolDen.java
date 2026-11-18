package com.grahambartley.nightborn.tools.structures.vampire.lair;

import static com.grahambartley.nightborn.tools.structures.vampire.lair.LairShrine.BLACKSTONE_COFFIN;
import static com.grahambartley.nightborn.tools.structures.vampire.lair.LairShrine.DEEPSLATE_COFFIN;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Furnishing;

public final class BloodPoolDen {

  public static final String PATH = "vampire/lair/den";
  public static final int SIZE = 25;
  public static final int SIZE_Y = 12;
  public static final int CENTRE = 12;

  private BloodPoolDen() {}

  public static Template build() {
    final Template t = new Template(PATH, SIZE, SIZE_Y, SIZE);
    LairCave.carve(t);
    LairFeatures.path(t, 5, 16, 9, 17);
    LairFeatures.path(t, 9, 15, 10, 16);
    LairFeatures.pool(t, CENTRE, CENTRE, 2.5, 2.5);
    t.set(CENTRE, 0, CENTRE, LairFeatures.RIM);
    t.set(
        CENTRE,
        1,
        CENTRE,
        BlockState.of("nightborn:blood_fountain", "facing", "south", "flowing", true));
    LairShrine.cut(t, 6, 2, "south", DEEPSLATE_COFFIN);
    LairShrine.cut(t, 18, 2, "south", BLACKSTONE_COFFIN);
    LairShrine.cut(t, 22, CENTRE, "west", DEEPSLATE_COFFIN);
    LairShrine.cut(t, 2, 6, "east", BLACKSTONE_COFFIN);
    LairShrine.cut(t, 22, 17, "west", BLACKSTONE_COFFIN);
    gathering(t);
    altar(t);
    remains(t);
    LairFeatures.readingCorner(t, 20, 18, 5, "west");
    return t;
  }

  private static void gathering(final Template t) {
    Furnishing.chair(t, CENTRE, 1, CENTRE - 5, "south");
    Furnishing.chair(t, CENTRE + 5, 1, CENTRE, "west");
    Furnishing.chair(t, CENTRE - 5, 1, CENTRE, "east");
    Furnishing.candelabra(t, CENTRE - 4, 1, CENTRE - 4);
    Furnishing.candelabra(t, CENTRE + 4, 1, CENTRE + 4);
    Furnishing.gramophone(t, CENTRE + 4, 1, CENTRE - 4, "west");
    Furnishing.oldBloodFont(t, CENTRE - 4, 1, CENTRE - 2);
    t.set(CENTRE + 4, 1, CENTRE - 1, Furnishing.splatter("west", 1));
    t.set(CENTRE - 1, 1, CENTRE - 4, Furnishing.splatter("north", 3));
    t.set(CENTRE - 4, 1, CENTRE + 2, Furnishing.splatter("east", 0));
  }

  private static void altar(final Template t) {
    for (int x = 3; x <= 5; x++) {
      t.set(x, 1, 18, "polished_blackstone");
    }
    Furnishing.chalice(t, 4, 2, 18, "player");
    Furnishing.candles(t, 3, 2, 18, 3, false);
    Furnishing.candles(t, 5, 2, 18, 2, false);
    Furnishing.chest(t, 4, 1, 19, "north");
  }

  private static void remains(final Template t) {
    LairFeatures.bones(t, 8, 21, 2);
    LairFeatures.bones(t, 16, 4, 10);
    LairFeatures.stalagmite(t, 19, 16, 2);
    LairFeatures.stalagmite(t, 4, 12, 1);
    LairFeatures.stalagmite(t, 15, 21, 3);
    Furnishing.chest(t, 10, 1, 21, "north");
    t.set(6, 3, 2, "cobweb");
    t.set(22, 3, CENTRE, "cobweb");
  }
}
