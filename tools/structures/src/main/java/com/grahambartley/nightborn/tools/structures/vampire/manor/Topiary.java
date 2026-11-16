package com.grahambartley.nightborn.tools.structures.vampire.manor;

import com.grahambartley.nightborn.tools.structures.template.Template;

public final class Topiary {

  public static final String CLIPPED = Parts.leaves("azalea_leaves");
  public static final int FIGURE_HEIGHT = 5;

  private Topiary() {}

  public static void figure(final Template t, final int x, final int z) {
    t.set(x, 0, z, "moss_block");
    for (int dx = -1; dx <= 1; dx++) {
      for (int dz = -1; dz <= 1; dz++) {
        if (dx == 0 || dz == 0) {
          t.set(x + dx, 1, z + dz, CLIPPED);
        }
      }
    }
    t.set(x, 2, z, CLIPPED);
    t.set(x, 2, z - 1, CLIPPED);
    t.set(x, 2, z + 1, CLIPPED);
    t.set(x, 3, z, CLIPPED);
    t.set(x - 1, 3, z, CLIPPED);
    t.set(x, 4, z, "moss_block");
    t.set(x, FIGURE_HEIGHT, z, CLIPPED);
  }

  public static void cone(final Template t, final int x, final int z) {
    t.set(x, 0, z, "moss_block");
    for (int dx = -1; dx <= 1; dx++) {
      for (int dz = -1; dz <= 1; dz++) {
        t.set(x + dx, 1, z + dz, CLIPPED);
      }
    }
    t.set(x, 2, z, CLIPPED);
    t.set(x, 3, z, CLIPPED);
  }
}
