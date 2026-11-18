package com.grahambartley.nightborn.rank;

import com.grahambartley.nightborn.faction.Oath;
import java.util.Optional;

public record RankOutlook(RankLadder.Rung next, int pointsLeft, boolean deedDone) {

  public static Optional<RankOutlook> of(
      final Oath oath, final int earned, final RankLadder ladder, final RankNeeds needs) {
    final int next = oath.rank() + 1;
    if (next > RankRules.top(ladder, needs)) {
      return Optional.empty();
    }
    final int points = needs.pointsFor(next).orElse(0);
    return ladder
        .rung(next)
        .map(
            rung ->
                new RankOutlook(
                    rung, Math.max(0, points - earned), RankRules.deedDone(oath, next, ladder)));
  }

  public Optional<RankDeed> deed() {
    return next.rank().deed();
  }
}
