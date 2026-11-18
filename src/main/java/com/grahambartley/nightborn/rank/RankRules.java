package com.grahambartley.nightborn.rank;

import com.grahambartley.nightborn.faction.Oath;
import java.util.Collection;

public final class RankRules {

  private RankRules() {}

  public static int top(final RankLadder ladder, final RankNeeds needs) {
    return Math.max(0, Math.min(ladder.size() - 1, needs.size()));
  }

  public static boolean opened(final int earned, final int rank, final RankNeeds needs) {
    return needs.pointsFor(rank).stream().anyMatch(points -> earned >= points);
  }

  public static boolean deedDone(final Oath oath, final int rank, final RankLadder ladder) {
    return ladder.deed(rank).isEmpty() || oath.deedDone(rank);
  }

  public static boolean reaches(
      final Oath oath,
      final int earned,
      final int rank,
      final RankLadder ladder,
      final RankNeeds needs) {
    return rank >= 1
        && rank <= top(ladder, needs)
        && opened(earned, rank, needs)
        && deedDone(oath, rank, ladder);
  }

  public static Oath advance(
      final Oath oath,
      final boolean vampire,
      final int earned,
      final RankLadder ladder,
      final RankNeeds needs) {
    if (!oath.hunter(vampire)) {
      return oath;
    }
    int rank = oath.rank();
    while (reaches(oath, earned, rank + 1, ladder, needs)) {
      rank++;
    }
    return rank == oath.rank() ? oath : oath.withRank(rank);
  }

  public static Oath did(
      final Oath oath,
      final RankLadder ladder,
      final Collection<RankDeed.Act> acts,
      final int stageRank) {
    Oath done = oath;
    for (int rank = 0; rank < ladder.size(); rank++) {
      if (ladder
          .deed(rank)
          .filter(deed -> acts.stream().anyMatch(act -> deed.doneBy(act, stageRank)))
          .isPresent()) {
        done = done.withDeed(rank);
      }
    }
    return done;
  }
}
