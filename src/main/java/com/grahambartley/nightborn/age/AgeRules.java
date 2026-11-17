package com.grahambartley.nightborn.age;

import java.util.List;

public final class AgeRules {

  private AgeRules() {}

  public static int top(final int ladderSize, final List<AgeNeeds> needs) {
    return Math.max(0, Math.min(ladderSize - 1, needs.size()));
  }

  public static boolean reaches(
      final AgeProgress progress, final int rank, final List<AgeNeeds> needs) {
    return rank >= 1
        && rank <= needs.size()
        && needs.get(rank - 1).opened(progress)
        && progress.deedDone(rank);
  }

  public static AgeProgress advance(
      final AgeProgress progress, final int ladderSize, final List<AgeNeeds> needs) {
    final int top = top(ladderSize, needs);
    int rank = progress.rank();
    while (rank < top && reaches(progress, rank + 1, needs)) {
      rank++;
    }
    return rank == progress.rank() ? progress : progress.withRank(rank);
  }
}
