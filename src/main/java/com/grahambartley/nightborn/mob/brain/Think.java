package com.grahambartley.nightborn.mob.brain;

import java.util.List;
import java.util.Optional;

public record Think(
    Intent intent, long tick, List<PowerChoice.Scored> ranked, IntentBudget budget, double roll) {

  public Think {
    ranked = List.copyOf(ranked);
  }

  public Optional<PowerChoice.Scored> chosen() {
    return PowerChoice.choose(ranked, budget, roll);
  }
}
