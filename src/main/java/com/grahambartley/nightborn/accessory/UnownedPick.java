package com.grahambartley.nightborn.accessory;

import java.util.List;
import java.util.Set;

public final class UnownedPick {

  private UnownedPick() {}

  public static <T> List<T> candidates(final List<T> pieces, final List<Set<T>> ownedByEach) {
    final List<T> fair =
        pieces.stream()
            .filter(piece -> ownedByEach.stream().noneMatch(owned -> unfair(piece, owned, pieces)))
            .toList();
    return fair.isEmpty() ? pieces : fair;
  }

  static <T> boolean unfair(final T piece, final Set<T> owned, final List<T> pieces) {
    return owned.contains(piece) && !owned.containsAll(pieces);
  }
}
