package com.grahambartley.nightborn.balance;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record RoundResult(
    String matchup,
    Optional<Side> winner,
    int ticks,
    Map<Side, Tally.Blows> blows,
    Map<Side, Set<String>> tags) {

  public Tally.Blows blowsOf(final Side side) {
    return blows.getOrDefault(side, Tally.Blows.NONE);
  }

  public Set<String> tagsOf(final Side side) {
    return tags.getOrDefault(side, Set.of());
  }

  public double seconds() {
    return ticks / 20.0;
  }
}
