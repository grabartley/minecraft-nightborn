package com.grahambartley.nightborn.rank;

import java.util.List;
import java.util.OptionalInt;

public record RankNeeds(List<Integer> points) {

  public RankNeeds {
    points = List.copyOf(points);
  }

  public int size() {
    return points.size();
  }

  public OptionalInt pointsFor(final int rank) {
    if (rank == 0) {
      return OptionalInt.of(0);
    }
    return rank >= 1 && rank <= points.size()
        ? OptionalInt.of(Math.max(0, points.get(rank - 1)))
        : OptionalInt.empty();
  }
}
