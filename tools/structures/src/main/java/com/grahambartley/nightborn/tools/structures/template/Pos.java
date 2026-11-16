package com.grahambartley.nightborn.tools.structures.template;

import java.util.Comparator;

public record Pos(int x, int y, int z) {

  public static final Comparator<Pos> SAVE_ORDER =
      Comparator.comparingInt(Pos::y).thenComparingInt(Pos::x).thenComparingInt(Pos::z);

  public Pos offset(final int dx, final int dy, final int dz) {
    return new Pos(x + dx, y + dy, z + dz);
  }

  public Pos offset(final Pos step) {
    return offset(step.x, step.y, step.z);
  }
}
