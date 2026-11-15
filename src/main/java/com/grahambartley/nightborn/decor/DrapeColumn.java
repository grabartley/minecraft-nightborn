package com.grahambartley.nightborn.decor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;

public final class DrapeColumn {

  private DrapeColumn() {}

  public static List<Integer> offsets(final IntPredicate matchesAt) {
    final List<Integer> offsets = new ArrayList<>();
    offsets.add(0);
    for (int up = 1; matchesAt.test(up); up++) {
      offsets.add(up);
    }
    for (int down = -1; matchesAt.test(down); down--) {
      offsets.add(down);
    }
    return List.copyOf(offsets);
  }
}
