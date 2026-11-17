package com.grahambartley.nightborn.tools.structures.vampire.manor;

import com.grahambartley.nightborn.tools.structures.template.Facing;
import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;

public record Window(int x, int z, String outward, int sill, int height, String glass) {

  public static final int GROUND_SILL = Manor.F0 + 2;
  public static final int UPPER_SILL = Manor.F1 + 1;
  public static final int TALL = 3;

  public static Window ground(final int x, final int z, final String outward) {
    return new Window(x, z, outward, GROUND_SILL, TALL, Manor.PANE);
  }

  public static Window upper(final int x, final int z, final String outward) {
    return new Window(x, z, outward, UPPER_SILL, TALL, Manor.PANE);
  }

  public int head() {
    return sill + height;
  }

  public void cut(final Template t) {
    for (int y = sill; y < head(); y++) {
      t.set(x, y, z, glass);
    }
    final Pos out = Facing.STEP.get(outward);
    final int ox = x + out.x();
    final int oz = z + out.z();
    if (t.inside(ox, head(), oz)) {
      t.set(ox, head(), oz, Parts.hood(Manor.TRIM, Facing.OPPOSITE.get(outward)));
      t.set(ox, sill - 1, oz, Parts.slab(Manor.TRIM_SLAB, "top"));
    }
  }

  public static void door(
      final Template t, final int x, final int y, final int z, final String facing) {
    t.set(x, y, z, Parts.door(Manor.DOOR, facing, "lower", "left"));
    t.set(x, y + 1, z, Parts.door(Manor.DOOR, facing, "upper", "left"));
  }
}
