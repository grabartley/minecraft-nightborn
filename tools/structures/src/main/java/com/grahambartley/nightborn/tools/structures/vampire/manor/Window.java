package com.grahambartley.nightborn.tools.structures.vampire.manor;

import com.grahambartley.nightborn.tools.structures.template.Facing;
import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Furnishing;

public record Window(
    int x, int z, String outward, int sill, int height, String glass, String drape) {

  public static final int GROUND_SILL = Manor.F0 + 2;
  public static final int GROUND_TALL = 4;
  public static final int UPPER_SILL = Manor.F1 + 2;
  public static final int UPPER_TALL = 3;

  public static Window ground(final int x, final int z, final String outward, final String drape) {
    return new Window(x, z, outward, GROUND_SILL, GROUND_TALL, Manor.PANE, drape);
  }

  public static Window upper(final int x, final int z, final String outward, final String drape) {
    return new Window(x, z, outward, UPPER_SILL, UPPER_TALL, Manor.PANE, drape);
  }

  public Window stained() {
    return new Window(x, z, outward, sill, height, Manor.STAINED, drape);
  }

  public Window rows(final int newSill, final int newHeight) {
    return new Window(x, z, outward, newSill, newHeight, glass, drape);
  }

  public int head() {
    return sill + height;
  }

  public Pos reveal() {
    final Pos out = Facing.STEP.get(outward);
    return new Pos(x - out.x(), sill, z - out.z());
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
    final Pos in = reveal();
    Furnishing.drape(t, drape, in.x(), sill, in.z(), height, Facing.OPPOSITE.get(outward));
  }
}
