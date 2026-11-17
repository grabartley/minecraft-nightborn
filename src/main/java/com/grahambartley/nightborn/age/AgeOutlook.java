package com.grahambartley.nightborn.age;

import java.util.List;
import java.util.Optional;

public record AgeOutlook(int next, int nightsLeft, int bloodLeft, boolean deedDone) {

  public static Optional<AgeOutlook> of(
      final AgeProgress progress, final int ladderSize, final List<AgeNeeds> needs) {
    final int next = progress.rank() + 1;
    if (next > AgeRules.top(ladderSize, needs)) {
      return Optional.empty();
    }
    final AgeNeeds need = needs.get(next - 1);
    return Optional.of(
        new AgeOutlook(
            next,
            Math.max(0, need.nights() - progress.nights()),
            Math.max(0, need.blood() - progress.blood()),
            progress.deedDone(next)));
  }

  public boolean opened() {
    return nightsLeft == 0 && bloodLeft == 0;
  }
}
